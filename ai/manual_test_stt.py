import asyncio
import sys
import os
import io

# Add project root to path ensuring 'app' can be imported
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.append(current_dir)

# Ensure dotenv is loaded if it's not handled automatically by pydantic settings yet when running script
from dotenv import load_dotenv
load_dotenv()

try:
    from app.services.stt_service import STTProcessingService
    from pydub import AudioSegment
except ImportError as e:
    print(f"ImportError: {e}")
    print("Ensure you are running this script from the project root or PYTHONPATH is set.")
    print("Also ensure 'pydub' is installed: pip install pydub")
    sys.exit(1)

async def main():
    file_path = os.path.join("tests", "audio_sample.mp4")
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return

    print(f"Reading {file_path}...")
    
    # Check file size
    file_size = os.path.getsize(file_path)
    print(f"Original file size: {file_size / (1024*1024):.2f} MB")

    audio_data = None
    
    # OpenAI Whisper limit is 25MB. If larger, we must slice/compress.
    if file_size > 25 * 1024 * 1024:
        print("File exceeds 25MB limit. Attempting to slice first 30 seconds using pydub...")
        try:
            # Load audio from file (pydub uses ffmpeg/avconv)
            audio = AudioSegment.from_file(file_path)
            
            # Slice first 30 seconds (30 * 1000 ms)
            # OpenAI costs are per minute, smaller is better for test.
            sliced_audio = audio[:30000] 
            
            # Export to memory as WAV (because stt_service names it input.wav)
            buffer = io.BytesIO()
            sliced_audio.export(buffer, format="wav")
            audio_data = buffer.getvalue()
            
            print(f"Sliced audio size: {len(audio_data) / (1024*1024):.2f} MB")
            
        except Exception as e:
            print(f"Failed to slice audio with pydub: {e}")
            print("Please ensure ffmpeg is installed and in your PATH.")
            print("Falling back to reading raw file (will likely fail API call)...")
            # Fallback
            with open(file_path, "rb") as f:
                audio_data = f.read()
    else:
        # File is small enough, read directly
        try:
            with open(file_path, "rb") as f:
                audio_data = f.read()
        except Exception as e:
            print(f"Failed to read file: {e}")
            return

    print(f"Sending {len(audio_data)} bytes. Calling STTProcessingService...")
    
    session_id = "manual_test_123"
    timeline = 1

    try:
        text, timeline_res = await STTProcessingService.process_audio_chunk(
            session_id, timeline, audio_data
        )
        print("\n--- STT Service Result ---")
        print(f"Timeline: {timeline_res}")
        print(f"Text Content: {text}")
        print("--------------------------")
        
    except Exception as e:
        print(f"\nError occurred during processing: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    asyncio.run(main())
