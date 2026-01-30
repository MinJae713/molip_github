import os
import io
import traceback
import time
import uuid
import asyncio
from app.models.model_loader import models
from app.db.chroma_client import get_vectorstore
from app.core.logger import log

class STTProcessingService:
    @staticmethod
    async def process_audio_chunk(session_id: str, timeline: int, audio_bytes: bytes):
        log.debug(f"--- 서비스 시작: session_id={session_id}, timeline={timeline} ---")
        log.debug(f"수신된 오디오 크기: {len(audio_bytes)} bytes")
        start_total = time.perf_counter()

        client = models.get_openai_client()
        vectorstore = get_vectorstore()

        # 1. 메모리 내 바이너리 데이터를 파일 객체처럼 포장
        try:
            start_step = time.perf_counter()
            audio_file = io.BytesIO(audio_bytes)
            audio_file.name = "input.wav"
            audio_proc_time = time.perf_counter() - start_step
            log.debug("1. 오디오 파일 객체 생성 완료")
        except Exception as e:
            log.error(f"오디오 객체 생성 중 오류: {e}")
            return "", timeline

        try:
            # 2. Whisper API 호출
            log.debug("2. Whisper API 호출 시작 (데이터 전송 중...)")
            start_step = time.perf_counter()
            raw_text = await client.audio.transcriptions.create(
                model="whisper-1",
                file=audio_file,
                response_format="text",
            )

            clean_text = raw_text.strip()
            stt_time = time.perf_counter() - start_step
            log.debug(f"2. STT 변환 성공: '{clean_text}'")

            if not clean_text:
                log.warning("변환된 텍스트가 비어 있습니다.")
                return "", timeline

            # 3. LLM 오타 수정 (후처리)
            log.debug("3. LLM 교정 시작...")
            start_step = time.perf_counter()
            corrected_text = await STTProcessingService._correct_typos(client, clean_text)
            llm_time = time.perf_counter() - start_step
            log.debug(f"3. LLM 교정 완료: '{corrected_text}'")

            # 4. ChromaDB 저장
            log.debug("4. ChromaDB 저장 시도...")
            start_step = time.perf_counter()
            doc_id = await STTProcessingService._save_to_chroma(vectorstore, session_id, timeline, clean_text, corrected_text)
            db_time = time.perf_counter() - start_step
            log.debug(f"4. DB 저장 완료 (ID: {doc_id})")
            
            total_time = time.perf_counter() - start_total

            # 결과 출력
            log.info(f"\n⏱️ [LATENCY REPORT - {session_id}]")
            log.info(f"1. Audio Wrap : {audio_proc_time:.4f}s")
            log.info(f"2. Whisper STT: {stt_time:.4f}s")
            log.info(f"3. LLM Correct: {llm_time:.4f}s")
            log.info(f"4. DB Storage : {db_time:.4f}s")
            log.info(f"🚀 TOTAL SERVER LATENCY: {total_time:.4f}s")
            return corrected_text, timeline

        except Exception as e:
            log.critical(f"처리 중 예외 발생: {type(e).__name__} - {e}")
            log.debug(traceback.format_exc())
            return "", timeline

    @staticmethod
    async def _correct_typos(client, text: str) -> str:
        try:
            response = await client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": (
                        "너는 전문적인 강의 자막 교정 전문가야. "
                        "제공된 텍스트의 문맥을 파악해서 오타와 맞춤법을 수정해줘. "
                        "최대한 원문의 뜻을 유지하되, 자연스러운 문장으로 다듬어줘. "
                        "결과물은 오직 수정된 텍스트만 출력해."
                    )},
                    {"role": "user", "content": text}
                ],
                temperature=0,
            )
            return response.choices[0].message.content.strip()
        except Exception as e:
            log.error(f"LLM 교정 실패: {e}")
            return text

    @staticmethod
    async def _save_to_chroma(vectorstore, session_id, timeline, original, corrected) -> str:
        doc_id = str(uuid.uuid4())
        await asyncio.to_thread(
            vectorstore.add_texts,
            texts=[corrected],
            metadatas=[{"session_id": session_id, "timeline": timeline, "original_text": original}],
            ids=[doc_id],
        )
        return doc_id