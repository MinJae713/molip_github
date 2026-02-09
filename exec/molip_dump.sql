-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: molip
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_analysis_timeline`
--

DROP TABLE IF EXISTS `ai_analysis_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_analysis_timeline` (
  `ai_analysis_timeline_id` bigint NOT NULL AUTO_INCREMENT,
  `concentrate_value` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deadzone` bit(1) NOT NULL,
  `emotion_name` varchar(31) COLLATE utf8mb4_unicode_ci NOT NULL,
  `emotion_value` float NOT NULL,
  `fever_time` bit(1) NOT NULL,
  `time` int NOT NULL,
  `session_id` bigint NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`ai_analysis_timeline_id`),
  KEY `FKa78ie3opjdl2cvko9ltsawnmu` (`session_id`),
  KEY `FKqkd2dq0340vchcq8jo9nhgi2s` (`workspace_member_id`),
  CONSTRAINT `FKa78ie3opjdl2cvko9ltsawnmu` FOREIGN KEY (`session_id`) REFERENCES `leaner_analysis` (`session_id`),
  CONSTRAINT `FKqkd2dq0340vchcq8jo9nhgi2s` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_analysis_timeline`
--

LOCK TABLES `ai_analysis_timeline` WRITE;
/*!40000 ALTER TABLE `ai_analysis_timeline` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_analysis_timeline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_deadzone_quiz`
--

DROP TABLE IF EXISTS `ai_deadzone_quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_deadzone_quiz` (
  `quiz_id` bigint NOT NULL AUTO_INCREMENT,
  `answer_number` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `end_time` int NOT NULL,
  `question` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_time` int NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`quiz_id`),
  KEY `FKkdkej2nbqpntd6t95lbqchuuw` (`session_id`),
  CONSTRAINT `FKkdkej2nbqpntd6t95lbqchuuw` FOREIGN KEY (`session_id`) REFERENCES `leaner_analysis` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_deadzone_quiz`
--

LOCK TABLES `ai_deadzone_quiz` WRITE;
/*!40000 ALTER TABLE `ai_deadzone_quiz` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_deadzone_quiz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `aiquiz_answer`
--

DROP TABLE IF EXISTS `aiquiz_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `aiquiz_answer` (
  `ai_quiz_answer_id` bigint NOT NULL AUTO_INCREMENT,
  `answer` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `number` int NOT NULL,
  `quiz_id` bigint NOT NULL,
  PRIMARY KEY (`ai_quiz_answer_id`),
  KEY `FKe5wum1nm3094dtnkb59ao5085` (`quiz_id`),
  CONSTRAINT `FKe5wum1nm3094dtnkb59ao5085` FOREIGN KEY (`quiz_id`) REFERENCES `ai_deadzone_quiz` (`quiz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aiquiz_answer`
--

LOCK TABLES `aiquiz_answer` WRITE;
/*!40000 ALTER TABLE `aiquiz_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `aiquiz_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attachment`
--

DROP TABLE IF EXISTS `attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `extension_type` enum('DOCX','JPG','PDF','PNG','ZIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `size` bigint NOT NULL,
  `size_unit` enum('B','GB','KB','MB','TB') COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('COMMON','SESSION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `upload_date` datetime(6) NOT NULL,
  `url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attachment`
--

LOCK TABLES `attachment` WRITE;
/*!40000 ALTER TABLE `attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `certification`
--

DROP TABLE IF EXISTS `certification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `certification` (
  `certification_id` bigint NOT NULL AUTO_INCREMENT,
  `certificated_at` datetime(6) NOT NULL,
  `certificated_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `issuing_agency` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `qualification_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_certificated_id` bigint NOT NULL,
  PRIMARY KEY (`certification_id`),
  UNIQUE KEY `UKtf00mg3c3qnfwshxwmvsadrv1` (`course_certificated_id`),
  CONSTRAINT `FKgyqvwto0bbinxl3l89rys4sno` FOREIGN KEY (`course_certificated_id`) REFERENCES `course_certificated` (`course_certificated_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `certification`
--

LOCK TABLES `certification` WRITE;
/*!40000 ALTER TABLE `certification` DISABLE KEYS */;
/*!40000 ALTER TABLE `certification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `common_attachment`
--

DROP TABLE IF EXISTS `common_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `common_attachment` (
  `attachment_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `FKdvv76cdpdt718ppi174x57a62` (`course_id`),
  CONSTRAINT `FKdvv76cdpdt718ppi174x57a62` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `FKthi4hdx4337ok4rjama6yosyv` FOREIGN KEY (`attachment_id`) REFERENCES `attachment` (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `common_attachment`
--

LOCK TABLES `common_attachment` WRITE;
/*!40000 ALTER TABLE `common_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `common_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `course_id` bigint NOT NULL AUTO_INCREMENT,
  `attendance` int NOT NULL,
  `avg_attendance` float DEFAULT NULL,
  `avg_concentrate` float DEFAULT NULL,
  `avg_quiz_correct` float DEFAULT NULL,
  `category` enum('AI','DATA','DEV','MARKETING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `end_session_count` int NOT NULL,
  `engagement` int DEFAULT NULL,
  `exit_quiz` int DEFAULT NULL,
  `is_engage_check` bit(1) NOT NULL,
  `is_sentiment_analysis` bit(1) NOT NULL,
  `max_learner` int NOT NULL,
  `period` int NOT NULL,
  `score_top3` varchar(127) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('FINISH','INVITE','PREPARING','PROCEED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `sub_category` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `workspace_member_id` bigint NOT NULL,
  `workspace_id` bigint NOT NULL,
  PRIMARY KEY (`course_id`),
  KEY `FKi9qogtkg740pbdje7ilii27yb` (`workspace_member_id`),
  KEY `FK6ahh5fkf2uroxaavgoolpc5ko` (`workspace_id`),
  CONSTRAINT `FK6ahh5fkf2uroxaavgoolpc5ko` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`workspace_id`),
  CONSTRAINT `FKi9qogtkg740pbdje7ilii27yb` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,80,NULL,NULL,NULL,'DEV','2026-02-04 14:34:36.037933','스프링 부트의 기초부터 심화까지 다루는 과정입니다.',0,70,60,_binary '',_binary '',30,8,'{}','PREPARING','Backend','Spring Boot 완벽 가이드','2026-02-04 14:34:36.037952',1,1),(2,80,17.7778,5.5914,0,'DEV','2026-02-04 14:43:02.716589','스프링 부트의 기초부터 심화까지 다루는 과정입니다.',30,70,60,_binary '',_binary '',30,8,'{}','PREPARING','Backend','Spring Boot 완벽 가이드-teacher꺼꺼','2026-02-06 13:24:11.808051',2,1),(3,80,NULL,NULL,NULL,'DEV','2026-02-04 14:53:07.736251','스프링 부트와 JPA를 활용하여 실제 서비스 수준의 백엔드를 구축합니다.',0,70,60,_binary '',_binary '',30,8,'{}','PREPARING','Java/Spring','실전! 스프링 부트와 JPA 활용','2026-02-04 14:53:07.736270',5,1),(4,80,NULL,NULL,NULL,'MARKETING','2026-02-04 16:49:58.438912','으아아아아아아악',0,70,10,_binary '',_binary '',11,44,'{}','PREPARING','231','내가만든쿠키','2026-02-04 16:49:58.438936',1,1),(5,80,0,0,0,'DATA','2026-02-06 10:32:22.923197','테스트과정생성',1,70,70,_binary '',_binary '',10,10,'{}','PREPARING','Test','실전 마케팅 테스트','2026-02-07 16:14:38.586321',2,1),(6,80,100,25.8042,0,'DEV','2026-02-06 12:50:00.337377','제로 트러스트 및 블록체인을 기반으로 정보보안 전략을 강의합니다.',1,70,50,_binary '',_binary '',25,4,'{}','PREPARING','security','제로 트러스트 및 블록체인 기반 정보보안 전략','2026-02-06 13:05:56.393395',2,1),(7,50,88,41.6,92,'MARKETING','2026-02-06 13:37:54.669302','SSAFY 15기 대외홍보용 강의 과정 입니다.',4,50,50,_binary '',_binary '',100,3,'{}','PROCEED','SSAYF_15기','SSAFY 15기 대외홍보','2026-02-06 14:55:28.898078',13,6),(8,80,NULL,NULL,NULL,'DEV','2026-02-07 17:44:28.345654','ㅇㅇㅇㅇ',0,70,60,_binary '',_binary '',20,4,'{}','PREPARING','Backend','ㅇㅇㅇ','2026-02-07 17:44:28.345672',2,1),(9,80,NULL,NULL,NULL,'MARKETING','2026-02-07 22:58:17.113448','강의생성테스트',0,70,70,_binary '',_binary '',15,4,'{}','PROCEED','마케팅입니다','강의생성','2026-02-07 22:58:17.113471',2,1),(10,80,26.6667,6.80269,0,'DEV','2026-02-08 15:59:27.752009','Kafka를 이용한 대규모 트래픽 분산 전략',5,70,100,_binary '',_binary '',100,3,'{}','PROCEED','Backend','Kafka를 이용한 대규모 트래픽 분산 전략','2026-02-08 21:18:50.643575',13,6);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_apply`
--

DROP TABLE IF EXISTS `course_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_apply` (
  `course_apply_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_applied` bit(1) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`course_apply_id`),
  KEY `FK1av0bpa3k75yfg0xc3pnhkmua` (`course_id`),
  KEY `FK5cmi7l47jcnfoo8qjjuhctadw` (`workspace_member_id`),
  CONSTRAINT `FK1av0bpa3k75yfg0xc3pnhkmua` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `FK5cmi7l47jcnfoo8qjjuhctadw` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_apply`
--

LOCK TABLES `course_apply` WRITE;
/*!40000 ALTER TABLE `course_apply` DISABLE KEYS */;
INSERT INTO `course_apply` VALUES (1,'2026-02-04 14:43:44.744712',_binary '',2,3),(2,'2026-02-04 14:58:58.168131',_binary '',1,3),(3,'2026-02-04 15:05:03.796708',_binary '\0',3,3),(4,'2026-02-04 15:09:52.474782',_binary '',1,6),(5,'2026-02-04 15:37:29.871803',_binary '\0',3,6),(6,'2026-02-04 15:53:44.734626',_binary '',2,6),(7,'2026-02-05 23:23:34.884269',_binary '',2,10),(8,'2026-02-05 23:25:22.413306',_binary '\0',4,10),(9,'2026-02-06 12:52:32.019443',_binary '',6,3),(10,'2026-02-06 14:01:45.156533',_binary '',7,14),(11,'2026-02-06 14:03:34.321131',_binary '',7,15),(12,'2026-02-06 14:04:34.001517',_binary '',7,17),(13,'2026-02-06 14:04:44.553952',_binary '',7,16),(14,'2026-02-06 17:26:28.080815',_binary '',7,18),(15,'2026-02-07 15:51:59.099152',_binary '',5,19),(16,'2026-02-07 17:48:58.134896',_binary '',8,19),(17,'2026-02-07 18:38:40.428567',_binary '\0',1,19),(18,'2026-02-07 22:08:23.598080',_binary '\0',4,3),(19,'2026-02-08 17:26:00.238414',_binary '',10,14),(20,'2026-02-08 17:26:20.525744',_binary '',10,15),(21,'2026-02-08 17:26:34.659101',_binary '',10,16),(22,'2026-02-08 17:26:50.828831',_binary '',10,17);
/*!40000 ALTER TABLE `course_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_certificated`
--

DROP TABLE IF EXISTS `course_certificated`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_certificated` (
  `course_certificated_id` bigint NOT NULL AUTO_INCREMENT,
  `average_participate` decimal(8,2) NOT NULL,
  `certificated_at` datetime(6) NOT NULL,
  `quiz_correct` decimal(8,2) NOT NULL,
  `score` int NOT NULL,
  `course_id` bigint NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`course_certificated_id`),
  KEY `FKjhm9t0x0nnkkx8d5yrc09tnxt` (`course_id`),
  KEY `FKoqisnqn5j89iw4h0nvs6evvi4` (`workspace_member_id`),
  CONSTRAINT `FKjhm9t0x0nnkkx8d5yrc09tnxt` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `FKoqisnqn5j89iw4h0nvs6evvi4` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_certificated`
--

LOCK TABLES `course_certificated` WRITE;
/*!40000 ALTER TABLE `course_certificated` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_certificated` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_learner`
--

DROP TABLE IF EXISTS `course_learner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_learner` (
  `course_learner_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`course_learner_id`),
  KEY `FK2woxollxp9vx71lwdri0tifqx` (`course_id`),
  KEY `FKe3ysivoci5pkm7aayrfvgcbt9` (`workspace_member_id`),
  CONSTRAINT `FK2woxollxp9vx71lwdri0tifqx` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
  CONSTRAINT `FKe3ysivoci5pkm7aayrfvgcbt9` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_learner`
--

LOCK TABLES `course_learner` WRITE;
/*!40000 ALTER TABLE `course_learner` DISABLE KEYS */;
INSERT INTO `course_learner` VALUES (1,'2026-02-04 14:44:10.375969','2026-02-04 14:44:10.376011',2,3),(2,'2026-02-04 15:02:26.283448','2026-02-04 15:02:26.283468',1,3),(3,'2026-02-04 15:14:32.486157','2026-02-04 15:14:32.486175',1,6),(4,'2026-02-06 09:57:05.762865','2026-02-06 09:57:05.762899',2,6),(5,'2026-02-06 10:59:55.702131','2026-02-06 10:59:55.702143',2,10),(6,'2026-02-06 14:06:52.183736','2026-02-06 14:06:52.183752',7,14),(7,'2026-02-06 14:06:54.752928','2026-02-06 14:06:54.752946',7,15),(8,'2026-02-06 14:07:31.412820','2026-02-06 14:07:31.412837',7,17),(9,'2026-02-06 14:07:32.888416','2026-02-06 14:07:32.888432',7,16),(10,'2026-02-06 17:32:07.835742','2026-02-06 17:32:07.835758',2,18),(11,'2026-02-07 16:00:11.037199','2026-02-07 16:00:11.037214',5,19),(12,'2026-02-07 17:49:09.606350','2026-02-07 17:49:09.606384',8,19),(13,'2026-02-08 17:30:56.605613','2026-02-08 17:30:56.605631',10,14),(14,'2026-02-08 17:30:57.410536','2026-02-08 17:30:57.410554',10,15),(15,'2026-02-08 17:30:57.948543','2026-02-08 17:30:57.948560',10,16),(16,'2026-02-08 17:30:58.442404','2026-02-08 17:30:58.442421',10,17);
/*!40000 ALTER TABLE `course_learner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_learner_analysis`
--

DROP TABLE IF EXISTS `course_learner_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_learner_analysis` (
  `course_learner_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `member_id` bigint NOT NULL,
  `sum_attendance` int DEFAULT NULL,
  `sum_concentrate` float DEFAULT NULL,
  `sum_quiz_correct` float DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `course_id` bigint DEFAULT NULL,
  PRIMARY KEY (`course_learner_id`),
  UNIQUE KEY `uk_course_member` (`course_id`,`member_id`),
  CONSTRAINT `FKh6vwwj23ytb08xc33yhmns94l` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_learner_analysis`
--

LOCK TABLES `course_learner_analysis` WRITE;
/*!40000 ALTER TABLE `course_learner_analysis` DISABLE KEYS */;
INSERT INTO `course_learner_analysis` VALUES (1,'2026-02-06 09:03:06',7,14,454.373,0,'2026-02-06 13:24:11.801831',2),(2,'2026-02-06 11:08:28',2,1,18.0211,0,'2026-02-06 11:08:28.159249',2),(3,'2026-02-06 11:08:28',10,1,30.8325,0,'2026-02-06 11:08:28.160156',2),(4,'2026-02-06 13:05:56',7,1,25.8042,0,'2026-02-06 13:05:56.387676',6),(5,'2026-02-06 16:55:33',14,75,37.4,95,'2026-02-06 16:55:33.000000',7),(6,'2026-02-06 16:57:19',15,100,35.1,100,'2026-02-06 16:57:19.000000',7),(7,'2026-02-06 16:57:09',16,50,33.9,50,'2026-02-06 16:57:09.000000',7),(8,'2026-02-06 16:57:09',17,100,31.8,75,'2026-02-06 16:57:09.000000',7),(9,'2026-02-06 16:57:06',18,75,39.2,80,'2026-02-06 16:57:06.000000',7),(10,'2026-02-08 19:43:33',14,2,54.8077,0,'2026-02-08 21:18:50.637905',10),(11,'2026-02-08 20:34:01',16,1,10,0,'2026-02-08 20:34:01.012597',10),(12,'2026-02-08 21:18:51',17,1,37.2326,0,'2026-02-08 21:18:50.636126',10);
/*!40000 ALTER TABLE `course_learner_analysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_learner_session`
--

DROP TABLE IF EXISTS `course_learner_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_learner_session` (
  `course_learner_session_id` bigint NOT NULL AUTO_INCREMENT,
  `attendance_time` int NOT NULL,
  `avg_concentrate` float NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `exit_ticket` int DEFAULT NULL,
  `is_attendance` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `course_learner_id` bigint DEFAULT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`course_learner_session_id`),
  KEY `FKn363ktgln09fpato4fvok2gxs` (`course_learner_id`),
  KEY `FKj2pauk5vt9pqoxb402voisoli` (`session_id`),
  CONSTRAINT `FKj2pauk5vt9pqoxb402voisoli` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`),
  CONSTRAINT `FKn363ktgln09fpato4fvok2gxs` FOREIGN KEY (`course_learner_id`) REFERENCES `course_learner` (`course_learner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_learner_session`
--

LOCK TABLES `course_learner_session` WRITE;
/*!40000 ALTER TABLE `course_learner_session` DISABLE KEYS */;
INSERT INTO `course_learner_session` VALUES (1,109,29.3357,'2026-02-06 09:03:05.543581',NULL,_binary '',7,'2026-02-06 09:03:05.543610',NULL,40),(2,71,16.8527,'2026-02-06 09:34:07.839236',NULL,_binary '',7,'2026-02-06 09:34:07.839249',NULL,44),(3,84,15.1406,'2026-02-06 09:39:24.216096',NULL,_binary '',7,'2026-02-06 09:39:24.216106',NULL,45),(4,51,29.8246,'2026-02-06 09:43:07.537305',NULL,_binary '',7,'2026-02-06 09:43:07.537314',NULL,46),(5,487,17.6825,'2026-02-06 10:01:06.642608',NULL,_binary '',7,'2026-02-06 10:01:06.642619',NULL,47),(6,224,29.3073,'2026-02-06 10:35:42.667671',NULL,_binary '',7,'2026-02-06 10:35:42.667683',NULL,48),(7,97,11.6203,'2026-02-06 11:08:28.156012',NULL,_binary '',7,'2026-02-06 11:08:28.156022',NULL,53),(8,119,18.0211,'2026-02-06 11:08:28.157049',NULL,_binary '',2,'2026-02-06 11:08:28.157057',NULL,53),(9,88,30.8325,'2026-02-06 11:08:28.157928',NULL,_binary '',10,'2026-02-06 11:08:28.157937',NULL,53),(10,50,20.3947,'2026-02-06 12:09:57.285246',NULL,_binary '',7,'2026-02-06 12:09:57.285254',NULL,55),(11,213,23.9096,'2026-02-06 12:24:00.824385',NULL,_binary '',7,'2026-02-06 12:24:00.824393',NULL,57),(12,683,54.7914,'2026-02-06 12:48:04.298541',NULL,_binary '',7,'2026-02-06 12:48:04.298562',NULL,58),(13,153,34.2772,'2026-02-06 12:57:18.769242',NULL,_binary '',7,'2026-02-06 12:57:18.769253',NULL,59),(14,28,36.2708,'2026-02-06 13:01:57.751939',NULL,_binary '\0',7,'2026-02-06 13:01:57.751951',NULL,61),(15,137,25.8042,'2026-02-06 13:05:56.386194',NULL,_binary '',7,'2026-02-06 13:05:56.386205',NULL,60),(16,115,34.5708,'2026-02-06 13:05:57.761783',NULL,_binary '',7,'2026-02-06 13:05:57.761796',NULL,62),(17,34,45.5,'2026-02-06 13:08:11.274286',NULL,_binary '',7,'2026-02-06 13:08:11.274299',NULL,63),(18,21,27.3056,'2026-02-06 13:15:49.685156',NULL,_binary '\0',7,'2026-02-06 13:15:49.685168',NULL,65),(19,40,27.5885,'2026-02-06 13:24:11.799521',NULL,_binary '',7,'2026-02-06 13:24:11.799534',NULL,67),(20,660,25.1,'2026-02-06 17:00:58.000000',NULL,_binary '',15,'2026-02-06 17:00:58.000000',NULL,68),(21,660,22,'2026-02-06 17:00:59.000000',NULL,_binary '',16,'2026-02-06 17:00:59.000000',NULL,68),(22,660,19.2,'2026-02-06 17:00:59.000000',NULL,_binary '',17,'2026-02-06 17:00:59.000000',NULL,68),(23,660,20.2,'2026-02-06 17:01:01.000000',NULL,_binary '',18,'2026-02-06 17:01:01.000000',NULL,68),(24,71,34.6,'2026-02-08 15:50:55.000000',50,_binary '',14,'2026-02-08 15:50:55.000000',6,68),(25,52,31.1,'2026-02-08 15:50:55.000000',100,_binary '',14,'2026-02-08 15:50:55.000000',6,75),(26,58,39.8,'2026-02-08 15:50:56.000000',100,_binary '',14,'2026-02-08 15:50:56.000000',6,82),(27,38,23,'2026-02-08 15:50:56.000000',75,_binary '',14,'2026-02-08 15:50:56.000000',6,83),(28,18,47.9,'2026-02-08 15:50:57.000000',100,_binary '',14,'2026-02-08 15:50:57.000000',6,85),(29,71,44.6,'2026-02-08 15:50:58.000000',80,_binary '',15,'2026-02-08 15:50:58.000000',7,68),(30,52,34.1,'2026-02-08 15:50:58.000000',90,_binary '',15,'2026-02-08 15:50:58.000000',7,75),(31,58,32.7,'2026-02-08 15:50:59.000000',100,_binary '',15,'2026-02-08 15:50:59.000000',7,82),(32,38,31.9,'2026-02-08 15:50:59.000000',75,_binary '',15,'2026-02-08 15:50:59.000000',7,83),(33,18,38,'2026-02-08 15:50:59.000000',100,_binary '',15,'2026-02-08 15:50:59.000000',7,85),(34,12,24.5179,'2026-02-08 19:43:33.184613',NULL,_binary '',14,'2026-02-08 19:43:33.184625',NULL,108),(35,1,10,'2026-02-08 20:34:01.011491',NULL,_binary '',16,'2026-02-08 20:34:01.011502',NULL,111),(36,176,37.2326,'2026-02-08 21:18:50.632686',NULL,_binary '',17,'2026-02-08 21:18:50.632699',NULL,117),(37,23,30.2898,'2026-02-08 21:18:50.634300',NULL,_binary '',14,'2026-02-08 21:18:50.634310',NULL,117);
/*!40000 ALTER TABLE `course_learner_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emotion_graph`
--

DROP TABLE IF EXISTS `emotion_graph`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emotion_graph` (
  `emotion_graph_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `member_id` bigint NOT NULL,
  `name` varchar(31) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` float NOT NULL,
  `session_id` bigint NOT NULL,
  `workspace_member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`emotion_graph_id`),
  KEY `FKqarmxo4309n7ekvtkvbp4s3uy` (`session_id`),
  KEY `FK6v2mr0uvf3iq8u6lc07yi6u9k` (`workspace_member_id`),
  CONSTRAINT `FK6v2mr0uvf3iq8u6lc07yi6u9k` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`),
  CONSTRAINT `FKqarmxo4309n7ekvtkvbp4s3uy` FOREIGN KEY (`session_id`) REFERENCES `leaner_analysis` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emotion_graph`
--

LOCK TABLES `emotion_graph` WRITE;
/*!40000 ALTER TABLE `emotion_graph` DISABLE KEYS */;
INSERT INTO `emotion_graph` VALUES (1,'2026-02-04 16:00:19.000000',10002,'happy',15,1,NULL),(2,'2026-02-04 16:00:19.000000',10002,'sad',5,1,NULL),(3,'2026-02-04 16:00:19.000000',10002,'angry',2.5,1,NULL),(4,'2026-02-04 16:00:19.000000',10002,'surprise',12.5,1,NULL),(5,'2026-02-04 16:00:19.000000',10002,'fear',1,1,NULL),(6,'2026-02-04 16:00:19.000000',10002,'disgust',0,1,NULL),(7,'2026-02-04 16:00:19.000000',10002,'neutral',64,1,NULL),(29,'2026-02-04 16:15:54.000000',10001,'happy',25,1,NULL),(30,'2026-02-04 16:15:54.000000',10001,'sad',0,1,NULL),(31,'2026-02-04 16:15:54.000000',10001,'angry',0.5,1,NULL),(32,'2026-02-04 16:15:54.000000',10001,'surprise',32.5,1,NULL),(33,'2026-02-04 16:15:54.000000',10001,'fear',1,1,NULL),(34,'2026-02-04 16:15:54.000000',10001,'disgust',5,1,NULL),(35,'2026-02-04 16:15:54.000000',10001,'neutral',40,1,NULL),(43,'2026-02-04 16:23:34.000000',4,'happy',25,1,NULL),(44,'2026-02-04 16:23:34.000000',4,'sad',0,1,NULL),(45,'2026-02-04 16:23:34.000000',4,'angry',0.5,1,NULL),(46,'2026-02-04 16:23:34.000000',4,'surprise',32.5,1,NULL),(47,'2026-02-04 16:23:34.000000',4,'fear',1,1,NULL),(48,'2026-02-04 16:23:34.000000',4,'disgust',5,1,NULL),(49,'2026-02-04 16:23:34.000000',4,'neutral',40,1,NULL),(50,'2026-02-05 13:42:06.070895',6,'happy',0,17,NULL),(51,'2026-02-05 13:42:06.072823',6,'sad',0,17,NULL),(52,'2026-02-05 13:42:06.073951',6,'angry',0,17,NULL),(53,'2026-02-05 13:42:06.075222',6,'surprise',0,17,NULL),(54,'2026-02-05 13:42:06.076335',6,'fear',0,17,NULL),(55,'2026-02-05 13:42:06.077851',6,'disgust',0,17,NULL),(56,'2026-02-05 13:42:06.079030',6,'neutral',0,17,NULL),(57,'2026-02-05 14:52:11.058844',6,'happy',0,13,NULL),(58,'2026-02-05 14:52:11.060434',6,'sad',0,13,NULL),(59,'2026-02-05 14:52:11.061733',6,'angry',0,13,NULL),(60,'2026-02-05 14:52:11.062962',6,'surprise',0,13,NULL),(61,'2026-02-05 14:52:11.064488',6,'fear',0,13,NULL),(62,'2026-02-05 14:52:11.065960',6,'disgust',0,13,NULL),(63,'2026-02-05 14:52:11.067350',6,'neutral',0,13,NULL),(64,'2026-02-05 15:17:51.863540',6,'happy',0,26,NULL),(65,'2026-02-05 15:17:51.865363',6,'sad',0,26,NULL),(66,'2026-02-05 15:17:51.866544',6,'angry',0,26,NULL),(67,'2026-02-05 15:17:51.867668',6,'surprise',0,26,NULL),(68,'2026-02-05 15:17:51.868776',6,'fear',0,26,NULL),(69,'2026-02-05 15:17:51.869937',6,'disgust',0,26,NULL),(70,'2026-02-05 15:17:51.871175',6,'neutral',0,26,NULL),(71,'2026-02-05 15:29:58.629576',6,'happy',0,29,NULL),(72,'2026-02-05 15:29:58.631422',6,'sad',0,29,NULL),(73,'2026-02-05 15:29:58.632564',6,'angry',0,29,NULL),(74,'2026-02-05 15:29:58.633552',6,'surprise',0,29,NULL),(75,'2026-02-05 15:29:58.634485',6,'fear',0,29,NULL),(76,'2026-02-05 15:29:58.635483',6,'disgust',0,29,NULL),(77,'2026-02-05 15:29:58.636553',6,'neutral',0,29,NULL),(78,'2026-02-05 16:46:21.015464',6,'happy',0,33,NULL),(79,'2026-02-05 16:46:21.017092',6,'sad',0,33,NULL),(80,'2026-02-05 16:46:21.018351',6,'angry',0,33,NULL),(81,'2026-02-05 16:46:21.019553',6,'surprise',0,33,NULL),(82,'2026-02-05 16:46:21.020811',6,'fear',0,33,NULL),(83,'2026-02-05 16:46:21.021936',6,'disgust',0,33,NULL),(84,'2026-02-05 16:46:21.023677',6,'neutral',0,33,NULL),(85,'2026-02-05 17:14:42.982228',6,'happy',0,34,NULL),(86,'2026-02-05 17:14:42.983704',6,'sad',0,34,NULL),(87,'2026-02-05 17:14:42.984914',6,'angry',0,34,NULL),(88,'2026-02-05 17:14:42.986118',6,'surprise',0,34,NULL),(89,'2026-02-05 17:14:42.987346',6,'fear',0,34,NULL),(90,'2026-02-05 17:14:42.988473',6,'disgust',0,34,NULL),(91,'2026-02-05 17:14:42.989516',6,'neutral',0,34,NULL),(92,'2026-02-05 17:16:32.420398',6,'happy',0,36,NULL),(93,'2026-02-05 17:16:32.422261',6,'sad',0,36,NULL),(94,'2026-02-05 17:16:32.423588',6,'angry',0,36,NULL),(95,'2026-02-05 17:16:32.424843',6,'surprise',0,36,NULL),(96,'2026-02-05 17:16:32.426092',6,'fear',0,36,NULL),(97,'2026-02-05 17:16:32.427396',6,'disgust',0,36,NULL),(98,'2026-02-05 17:16:32.428595',6,'neutral',0,36,NULL),(99,'2026-02-05 17:28:24.672729',6,'happy',0,37,NULL),(100,'2026-02-05 17:28:24.674155',6,'sad',0,37,NULL),(101,'2026-02-05 17:28:24.675323',6,'angry',0,37,NULL),(102,'2026-02-05 17:28:24.676381',6,'surprise',0,37,NULL),(103,'2026-02-05 17:28:24.677379',6,'fear',0,37,NULL),(104,'2026-02-05 17:28:24.678315',6,'disgust',0,37,NULL),(105,'2026-02-05 17:28:24.679252',6,'neutral',0,37,NULL),(106,'2026-02-06 08:54:33.314196',6,'happy',0,38,NULL),(107,'2026-02-06 08:54:33.315690',6,'sad',0,38,NULL),(108,'2026-02-06 08:54:33.316700',6,'angry',0,38,NULL),(109,'2026-02-06 08:54:33.317619',6,'surprise',0,38,NULL),(110,'2026-02-06 08:54:33.318848',6,'fear',0,38,NULL),(111,'2026-02-06 08:54:33.319921',6,'disgust',0,38,NULL),(112,'2026-02-06 08:54:33.321040',6,'neutral',0,38,NULL),(113,'2026-02-06 08:58:42.280196',6,'happy',0,39,NULL),(114,'2026-02-06 08:58:42.281279',6,'sad',0,39,NULL),(115,'2026-02-06 08:58:42.282276',6,'angry',0,39,NULL),(116,'2026-02-06 08:58:42.283200',6,'surprise',0,39,NULL),(117,'2026-02-06 08:58:42.284087',6,'fear',0,39,NULL),(118,'2026-02-06 08:58:42.285204',6,'disgust',0,39,NULL),(119,'2026-02-06 08:58:42.286215',6,'neutral',0,39,NULL),(120,'2026-02-06 09:03:05.521286',7,'happy',0.605253,40,NULL),(121,'2026-02-06 09:03:05.522906',7,'sad',13.4206,40,NULL),(122,'2026-02-06 09:03:05.524050',7,'angry',2.73101,40,NULL),(123,'2026-02-06 09:03:05.525194',7,'surprise',0.257342,40,NULL),(124,'2026-02-06 09:03:05.526388',7,'fear',32.3282,40,NULL),(125,'2026-02-06 09:03:05.527617',7,'disgust',0,40,NULL),(126,'2026-02-06 09:03:05.528775',7,'neutral',26.8746,40,NULL),(127,'2026-02-06 09:03:05.530039',6,'happy',0.605253,40,NULL),(128,'2026-02-06 09:03:05.531319',6,'sad',13.4206,40,NULL),(129,'2026-02-06 09:03:05.533330',6,'angry',2.73101,40,NULL),(130,'2026-02-06 09:03:05.534604',6,'surprise',0.257342,40,NULL),(131,'2026-02-06 09:03:05.535774',6,'fear',32.3282,40,NULL),(132,'2026-02-06 09:03:05.537373',6,'disgust',0,40,NULL),(133,'2026-02-06 09:03:05.538537',6,'neutral',26.8746,40,NULL),(134,'2026-02-06 09:31:06.784617',6,'happy',0,43,NULL),(135,'2026-02-06 09:31:06.785712',6,'sad',0,43,NULL),(136,'2026-02-06 09:31:06.786645',6,'angry',0,43,NULL),(137,'2026-02-06 09:31:06.787515',6,'surprise',0,43,NULL),(138,'2026-02-06 09:31:06.788394',6,'fear',0,43,NULL),(139,'2026-02-06 09:31:06.789278',6,'disgust',0,43,NULL),(140,'2026-02-06 09:31:06.790155',6,'neutral',0,43,NULL),(141,'2026-02-06 09:34:07.825265',7,'happy',2.9252,44,NULL),(142,'2026-02-06 09:34:07.826450',7,'sad',9.87063,44,NULL),(143,'2026-02-06 09:34:07.827525',7,'angry',12.7069,44,NULL),(144,'2026-02-06 09:34:07.828484',7,'surprise',0.787953,44,NULL),(145,'2026-02-06 09:34:07.829576',7,'fear',9.23095,44,NULL),(146,'2026-02-06 09:34:07.830458',7,'disgust',0,44,NULL),(147,'2026-02-06 09:34:07.831371',7,'neutral',42.3989,44,NULL),(148,'2026-02-06 09:34:07.832298',6,'happy',1.4626,44,NULL),(149,'2026-02-06 09:34:07.833290',6,'sad',4.93532,44,NULL),(150,'2026-02-06 09:34:07.834266',6,'angry',6.35343,44,NULL),(151,'2026-02-06 09:34:07.835250',6,'surprise',0.393976,44,NULL),(152,'2026-02-06 09:34:07.836324',6,'fear',4.61547,44,NULL),(153,'2026-02-06 09:34:07.837269',6,'disgust',0,44,NULL),(154,'2026-02-06 09:34:07.838247',6,'neutral',21.1994,44,NULL),(155,'2026-02-06 09:39:24.202552',7,'happy',5.03646,45,NULL),(156,'2026-02-06 09:39:24.203603',7,'sad',20.8251,45,NULL),(157,'2026-02-06 09:39:24.204506',7,'angry',11.1567,45,NULL),(158,'2026-02-06 09:39:24.205396',7,'surprise',0.453257,45,NULL),(159,'2026-02-06 09:39:24.206290',7,'fear',6.11309,45,NULL),(160,'2026-02-06 09:39:24.207366',7,'disgust',0,45,NULL),(161,'2026-02-06 09:39:24.208291',7,'neutral',36.3051,45,NULL),(162,'2026-02-06 09:39:24.209191',6,'happy',2.51823,45,NULL),(163,'2026-02-06 09:39:24.210068',6,'sad',10.4125,45,NULL),(164,'2026-02-06 09:39:24.211095',6,'angry',5.57834,45,NULL),(165,'2026-02-06 09:39:24.212189',6,'surprise',0.226629,45,NULL),(166,'2026-02-06 09:39:24.213313',6,'fear',3.05654,45,NULL),(167,'2026-02-06 09:39:24.214201',6,'disgust',0,45,NULL),(168,'2026-02-06 09:39:24.215107',6,'neutral',18.1525,45,NULL),(169,'2026-02-06 09:43:07.524573',7,'happy',0.333742,46,NULL),(170,'2026-02-06 09:43:07.525472',7,'sad',0.517362,46,NULL),(171,'2026-02-06 09:43:07.526386',7,'angry',2.99147,46,NULL),(172,'2026-02-06 09:43:07.527344',7,'surprise',5.0789,46,NULL),(173,'2026-02-06 09:43:07.528258',7,'fear',9.4954,46,NULL),(174,'2026-02-06 09:43:07.529131',7,'disgust',0,46,NULL),(175,'2026-02-06 09:43:07.529956',7,'neutral',16.1685,46,NULL),(176,'2026-02-06 09:43:07.530778',6,'happy',0.166871,46,NULL),(177,'2026-02-06 09:43:07.531678',6,'sad',0.258681,46,NULL),(178,'2026-02-06 09:43:07.532634',6,'angry',1.49574,46,NULL),(179,'2026-02-06 09:43:07.533557',6,'surprise',2.53945,46,NULL),(180,'2026-02-06 09:43:07.534396',6,'fear',4.7477,46,NULL),(181,'2026-02-06 09:43:07.535384',6,'disgust',0,46,NULL),(182,'2026-02-06 09:43:07.536341',6,'neutral',8.08426,46,NULL),(183,'2026-02-06 10:01:06.630418',7,'happy',1.32321,47,NULL),(184,'2026-02-06 10:01:06.631465',7,'sad',13.2975,47,NULL),(185,'2026-02-06 10:01:06.632273',7,'angry',7.47479,47,NULL),(186,'2026-02-06 10:01:06.633073',7,'surprise',0.579777,47,NULL),(187,'2026-02-06 10:01:06.633886',7,'fear',51.7042,47,NULL),(188,'2026-02-06 10:01:06.634730',7,'disgust',0.0681403,47,NULL),(189,'2026-02-06 10:01:06.635575',7,'neutral',21.3058,47,NULL),(190,'2026-02-06 10:01:06.636363',6,'happy',1.32321,47,NULL),(191,'2026-02-06 10:01:06.637155',6,'sad',13.2975,47,NULL),(192,'2026-02-06 10:01:06.637932',6,'angry',7.47479,47,NULL),(193,'2026-02-06 10:01:06.638766',6,'surprise',0.579777,47,NULL),(194,'2026-02-06 10:01:06.639788',6,'fear',51.7042,47,NULL),(195,'2026-02-06 10:01:06.640616',6,'disgust',0.0681403,47,NULL),(196,'2026-02-06 10:01:06.641503',6,'neutral',21.3058,47,NULL),(197,'2026-02-06 10:35:42.655465',7,'happy',1.89911,48,NULL),(198,'2026-02-06 10:35:42.656411',7,'sad',12.2868,48,NULL),(199,'2026-02-06 10:35:42.657298',7,'angry',4.32667,48,NULL),(200,'2026-02-06 10:35:42.658231',7,'surprise',1.97191,48,NULL),(201,'2026-02-06 10:35:42.659222',7,'fear',6.99078,48,NULL),(202,'2026-02-06 10:35:42.660142',7,'disgust',0,48,NULL),(203,'2026-02-06 10:35:42.661013',7,'neutral',23.184,48,NULL),(204,'2026-02-06 10:35:42.661843',6,'happy',1.89911,48,NULL),(205,'2026-02-06 10:35:42.662655',6,'sad',12.2868,48,NULL),(206,'2026-02-06 10:35:42.663476',6,'angry',4.32667,48,NULL),(207,'2026-02-06 10:35:42.664292',6,'surprise',1.97191,48,NULL),(208,'2026-02-06 10:35:42.665103',6,'fear',6.99078,48,NULL),(209,'2026-02-06 10:35:42.665862',6,'disgust',0,48,NULL),(210,'2026-02-06 10:35:42.666629',6,'neutral',23.184,48,NULL),(211,'2026-02-06 10:51:55.851877',6,'happy',0,51,NULL),(212,'2026-02-06 10:51:55.852905',6,'sad',0,51,NULL),(213,'2026-02-06 10:51:55.853716',6,'angry',0,51,NULL),(214,'2026-02-06 10:51:55.854458',6,'surprise',0,51,NULL),(215,'2026-02-06 10:51:55.855203',6,'fear',0,51,NULL),(216,'2026-02-06 10:51:55.856104',6,'disgust',0,51,NULL),(217,'2026-02-06 10:51:55.856838',6,'neutral',0,51,NULL),(218,'2026-02-06 11:08:28.131084',7,'happy',245.786,53,NULL),(219,'2026-02-06 11:08:28.132006',7,'sad',96.4863,53,NULL),(220,'2026-02-06 11:08:28.132874',7,'angry',28.0205,53,NULL),(221,'2026-02-06 11:08:28.133721',7,'surprise',66.1105,53,NULL),(222,'2026-02-06 11:08:28.134587',7,'fear',346.287,53,NULL),(223,'2026-02-06 11:08:28.135486',7,'disgust',1.13684,53,NULL),(224,'2026-02-06 11:08:28.136334',7,'neutral',586.921,53,NULL),(225,'2026-02-06 11:08:28.137246',2,'happy',119.994,53,NULL),(226,'2026-02-06 11:08:28.138124',2,'sad',227.317,53,NULL),(227,'2026-02-06 11:08:28.139354',2,'angry',76.2816,53,NULL),(228,'2026-02-06 11:08:28.140276',2,'surprise',12.8589,53,NULL),(229,'2026-02-06 11:08:28.141144',2,'fear',126.97,53,NULL),(230,'2026-02-06 11:08:28.142080',2,'disgust',0.495263,53,NULL),(231,'2026-02-06 11:08:28.143027',2,'neutral',520.786,53,NULL),(232,'2026-02-06 11:08:28.144122',10,'happy',95.5463,53,NULL),(233,'2026-02-06 11:08:28.145081',10,'sad',92.5674,53,NULL),(234,'2026-02-06 11:08:28.145930',10,'angry',13.1532,53,NULL),(235,'2026-02-06 11:08:28.146771',10,'surprise',5.11895,53,NULL),(236,'2026-02-06 11:08:28.147572',10,'fear',195.282,53,NULL),(237,'2026-02-06 11:08:28.148442',10,'disgust',0,53,NULL),(238,'2026-02-06 11:08:28.149252',10,'neutral',96.71,53,NULL),(239,'2026-02-06 11:08:28.150061',6,'happy',153.775,53,NULL),(240,'2026-02-06 11:08:28.150808',6,'sad',138.79,53,NULL),(241,'2026-02-06 11:08:28.151594',6,'angry',39.1518,53,NULL),(242,'2026-02-06 11:08:28.152349',6,'surprise',28.0295,53,NULL),(243,'2026-02-06 11:08:28.153204',6,'fear',222.846,53,NULL),(244,'2026-02-06 11:08:28.154109',6,'disgust',0.544035,53,NULL),(245,'2026-02-06 11:08:28.155009',6,'neutral',401.472,53,NULL),(246,'2026-02-06 12:09:57.273390',7,'happy',6.37193,55,NULL),(247,'2026-02-06 12:09:57.274229',7,'sad',6.45269,55,NULL),(248,'2026-02-06 12:09:57.275054',7,'angry',8.04989,55,NULL),(249,'2026-02-06 12:09:57.275795',7,'surprise',3.29108,55,NULL),(250,'2026-02-06 12:09:57.276629',7,'fear',6.91269,55,NULL),(251,'2026-02-06 12:09:57.277627',7,'disgust',0,55,NULL),(252,'2026-02-06 12:09:57.278554',7,'neutral',48.8895,55,NULL),(253,'2026-02-06 12:09:57.279445',6,'happy',6.37193,55,NULL),(254,'2026-02-06 12:09:57.280337',6,'sad',6.45269,55,NULL),(255,'2026-02-06 12:09:57.281128',6,'angry',8.04989,55,NULL),(256,'2026-02-06 12:09:57.281955',6,'surprise',3.29108,55,NULL),(257,'2026-02-06 12:09:57.282769',6,'fear',6.91269,55,NULL),(258,'2026-02-06 12:09:57.283524',6,'disgust',0,55,NULL),(259,'2026-02-06 12:09:57.284319',6,'neutral',48.8895,55,NULL),(260,'2026-02-06 12:24:00.811669',7,'happy',2.03279,57,NULL),(261,'2026-02-06 12:24:00.812610',7,'sad',21.1594,57,NULL),(262,'2026-02-06 12:24:00.813525',7,'angry',2.73778,57,NULL),(263,'2026-02-06 12:24:00.814443',7,'surprise',0.795619,57,NULL),(264,'2026-02-06 12:24:00.815226',7,'fear',17.349,57,NULL),(265,'2026-02-06 12:24:00.816055',7,'disgust',0.0937778,57,NULL),(266,'2026-02-06 12:24:00.816917',7,'neutral',37.2278,57,NULL),(267,'2026-02-06 12:24:00.817827',6,'happy',2.03279,57,NULL),(268,'2026-02-06 12:24:00.818784',6,'sad',21.1594,57,NULL),(269,'2026-02-06 12:24:00.819690',6,'angry',2.73778,57,NULL),(270,'2026-02-06 12:24:00.820688',6,'surprise',0.795619,57,NULL),(271,'2026-02-06 12:24:00.821539',6,'fear',17.349,57,NULL),(272,'2026-02-06 12:24:00.822345',6,'disgust',0.0937778,57,NULL),(273,'2026-02-06 12:24:00.823163',6,'neutral',37.2278,57,NULL),(274,'2026-02-06 12:48:04.265395',7,'happy',0.613026,58,NULL),(275,'2026-02-06 12:48:04.272409',7,'sad',10.2529,58,NULL),(276,'2026-02-06 12:48:04.274589',7,'angry',1.39432,58,NULL),(277,'2026-02-06 12:48:04.276358',7,'surprise',1.12311,58,NULL),(278,'2026-02-06 12:48:04.279067',7,'fear',25.1672,58,NULL),(279,'2026-02-06 12:48:04.280880',7,'disgust',0,58,NULL),(280,'2026-02-06 12:48:04.282762',7,'neutral',20.3331,58,NULL),(281,'2026-02-06 12:48:04.285061',6,'happy',0.613026,58,NULL),(282,'2026-02-06 12:48:04.286828',6,'sad',10.2529,58,NULL),(283,'2026-02-06 12:48:04.288606',6,'angry',1.39432,58,NULL),(284,'2026-02-06 12:48:04.290251',6,'surprise',1.12311,58,NULL),(285,'2026-02-06 12:48:04.291737',6,'fear',25.1672,58,NULL),(286,'2026-02-06 12:48:04.293550',6,'disgust',0,58,NULL),(287,'2026-02-06 12:48:04.295315',6,'neutral',20.3331,58,NULL),(288,'2026-02-06 12:57:18.749548',7,'happy',0.622698,59,NULL),(289,'2026-02-06 12:57:18.751815',7,'sad',6.30423,59,NULL),(290,'2026-02-06 12:57:18.753092',7,'angry',3.51059,59,NULL),(291,'2026-02-06 12:57:18.754516',7,'surprise',0.317871,59,NULL),(292,'2026-02-06 12:57:18.755633',7,'fear',17.7055,59,NULL),(293,'2026-02-06 12:57:18.757726',7,'disgust',0,59,NULL),(294,'2026-02-06 12:57:18.759224',7,'neutral',13.0635,59,NULL),(295,'2026-02-06 12:57:18.760549',6,'happy',0.311349,59,NULL),(296,'2026-02-06 12:57:18.761731',6,'sad',3.15212,59,NULL),(297,'2026-02-06 12:57:18.762852',6,'angry',1.7553,59,NULL),(298,'2026-02-06 12:57:18.764174',6,'surprise',0.158936,59,NULL),(299,'2026-02-06 12:57:18.765302',6,'fear',8.85273,59,NULL),(300,'2026-02-06 12:57:18.766475',6,'disgust',0,59,NULL),(301,'2026-02-06 12:57:18.767594',6,'neutral',6.53173,59,NULL),(302,'2026-02-06 13:01:57.733482',7,'happy',0.817863,61,NULL),(303,'2026-02-06 13:01:57.735278',7,'sad',1.28847,61,NULL),(304,'2026-02-06 13:01:57.736502',7,'angry',0.370534,61,NULL),(305,'2026-02-06 13:01:57.737690',7,'surprise',2.28985,61,NULL),(306,'2026-02-06 13:01:57.738852',7,'fear',14.1744,61,NULL),(307,'2026-02-06 13:01:57.740249',7,'disgust',0,61,NULL),(308,'2026-02-06 13:01:57.741510',7,'neutral',3.38122,61,NULL),(309,'2026-02-06 13:01:57.742618',6,'happy',0.817863,61,NULL),(310,'2026-02-06 13:01:57.743818',6,'sad',1.28847,61,NULL),(311,'2026-02-06 13:01:57.745159',6,'angry',0.370534,61,NULL),(312,'2026-02-06 13:01:57.746441',6,'surprise',2.28985,61,NULL),(313,'2026-02-06 13:01:57.747603',6,'fear',14.1744,61,NULL),(314,'2026-02-06 13:01:57.748848',6,'disgust',0,61,NULL),(315,'2026-02-06 13:01:57.750229',6,'neutral',3.38122,61,NULL),(316,'2026-02-06 13:05:56.370404',7,'happy',0.597728,60,NULL),(317,'2026-02-06 13:05:56.371523',7,'sad',3.70319,60,NULL),(318,'2026-02-06 13:05:56.372627',7,'angry',0.34248,60,NULL),(319,'2026-02-06 13:05:56.373552',7,'surprise',2.85303,60,NULL),(320,'2026-02-06 13:05:56.374533',7,'fear',11.2861,60,NULL),(321,'2026-02-06 13:05:56.375500',7,'disgust',0,60,NULL),(322,'2026-02-06 13:05:56.376581',7,'neutral',15.524,60,NULL),(323,'2026-02-06 13:05:56.377752',6,'happy',0.298864,60,NULL),(324,'2026-02-06 13:05:56.379062',6,'sad',1.85159,60,NULL),(325,'2026-02-06 13:05:56.380255',6,'angry',0.17124,60,NULL),(326,'2026-02-06 13:05:56.381489',6,'surprise',1.42651,60,NULL),(327,'2026-02-06 13:05:56.382676',6,'fear',5.64303,60,NULL),(328,'2026-02-06 13:05:56.383723',6,'disgust',0,60,NULL),(329,'2026-02-06 13:05:56.384714',6,'neutral',7.76198,60,NULL),(330,'2026-02-06 13:05:57.737634',7,'happy',1.40329,62,NULL),(331,'2026-02-06 13:05:57.739510',7,'sad',6.69534,62,NULL),(332,'2026-02-06 13:05:57.743571',7,'angry',3.2977,62,NULL),(333,'2026-02-06 13:05:57.744672',7,'surprise',0.617081,62,NULL),(334,'2026-02-06 13:05:57.745739',7,'fear',26.2814,62,NULL),(335,'2026-02-06 13:05:57.747846',7,'disgust',0,62,NULL),(336,'2026-02-06 13:05:57.749091',7,'neutral',31.3144,62,NULL),(337,'2026-02-06 13:05:57.750175',6,'happy',1.40329,62,NULL),(338,'2026-02-06 13:05:57.751232',6,'sad',6.69534,62,NULL),(339,'2026-02-06 13:05:57.754013',6,'angry',3.2977,62,NULL),(340,'2026-02-06 13:05:57.756166',6,'surprise',0.617081,62,NULL),(341,'2026-02-06 13:05:57.757258',6,'fear',26.2814,62,NULL),(342,'2026-02-06 13:05:57.758501',6,'disgust',0,62,NULL),(343,'2026-02-06 13:05:57.759671',6,'neutral',31.3144,62,NULL),(344,'2026-02-06 13:08:11.248000',7,'happy',1.26539,63,NULL),(345,'2026-02-06 13:08:11.252068',7,'sad',3.50592,63,NULL),(346,'2026-02-06 13:08:11.253723',7,'angry',0.405789,63,NULL),(347,'2026-02-06 13:08:11.255384',7,'surprise',0.453816,63,NULL),(348,'2026-02-06 13:08:11.256753',7,'fear',9.40868,63,NULL),(349,'2026-02-06 13:08:11.258307',7,'disgust',0,63,NULL),(350,'2026-02-06 13:08:11.260005',7,'neutral',15.0887,63,NULL),(351,'2026-02-06 13:08:11.261557',6,'happy',1.26539,63,NULL),(352,'2026-02-06 13:08:11.262945',6,'sad',3.50592,63,NULL),(353,'2026-02-06 13:08:11.264372',6,'angry',0.405789,63,NULL),(354,'2026-02-06 13:08:11.265883',6,'surprise',0.453816,63,NULL),(355,'2026-02-06 13:08:11.267683',6,'fear',9.40868,63,NULL),(356,'2026-02-06 13:08:11.269154',6,'disgust',0,63,NULL),(357,'2026-02-06 13:08:11.270759',6,'neutral',15.0887,63,NULL),(358,'2026-02-06 13:11:43.195538',6,'happy',0,64,NULL),(359,'2026-02-06 13:11:43.197432',6,'sad',0,64,NULL),(360,'2026-02-06 13:11:43.198646',6,'angry',0,64,NULL),(361,'2026-02-06 13:11:43.199879',6,'surprise',0,64,NULL),(362,'2026-02-06 13:11:43.201147',6,'fear',0,64,NULL),(363,'2026-02-06 13:11:43.202639',6,'disgust',0,64,NULL),(364,'2026-02-06 13:11:43.203936',6,'neutral',0,64,NULL),(365,'2026-02-06 13:15:49.667528',7,'happy',0.236909,65,NULL),(366,'2026-02-06 13:15:49.668846',7,'sad',1.19,65,NULL),(367,'2026-02-06 13:15:49.670256',7,'angry',0.365394,65,NULL),(368,'2026-02-06 13:15:49.671432',7,'surprise',0,65,NULL),(369,'2026-02-06 13:15:49.672495',7,'fear',38.3501,65,NULL),(370,'2026-02-06 13:15:49.674170',7,'disgust',0,65,NULL),(371,'2026-02-06 13:15:49.675315',7,'neutral',3.36958,65,NULL),(372,'2026-02-06 13:15:49.676328',6,'happy',0.236909,65,NULL),(373,'2026-02-06 13:15:49.677421',6,'sad',1.19,65,NULL),(374,'2026-02-06 13:15:49.678610',6,'angry',0.365394,65,NULL),(375,'2026-02-06 13:15:49.679846',6,'surprise',0,65,NULL),(376,'2026-02-06 13:15:49.681130',6,'fear',38.3501,65,NULL),(377,'2026-02-06 13:15:49.682558',6,'disgust',0,65,NULL),(378,'2026-02-06 13:15:49.683692',6,'neutral',3.36958,65,NULL),(379,'2026-02-06 13:21:25.867087',6,'happy',0,66,NULL),(380,'2026-02-06 13:21:25.868350',6,'sad',0,66,NULL),(381,'2026-02-06 13:21:25.869517',6,'angry',0,66,NULL),(382,'2026-02-06 13:21:25.870540',6,'surprise',0,66,NULL),(383,'2026-02-06 13:21:25.871687',6,'fear',0,66,NULL),(384,'2026-02-06 13:21:25.872804',6,'disgust',0,66,NULL),(385,'2026-02-06 13:21:25.873873',6,'neutral',0,66,NULL),(386,'2026-02-06 13:24:11.777600',7,'happy',1.50481,67,NULL),(387,'2026-02-06 13:24:11.779143',7,'sad',25.0774,67,NULL),(388,'2026-02-06 13:24:11.780416',7,'angry',1.94167,67,NULL),(389,'2026-02-06 13:24:11.781797',7,'surprise',0.987407,67,NULL),(390,'2026-02-06 13:24:11.783626',7,'fear',24.1283,67,NULL),(391,'2026-02-06 13:24:11.784783',7,'disgust',0,67,NULL),(392,'2026-02-06 13:24:11.785852',7,'neutral',34.1009,67,NULL),(393,'2026-02-06 13:24:11.787097',6,'happy',1.50481,67,NULL),(394,'2026-02-06 13:24:11.788372',6,'sad',25.0774,67,NULL),(395,'2026-02-06 13:24:11.789713',6,'angry',1.94167,67,NULL),(396,'2026-02-06 13:24:11.790893',6,'surprise',0.987407,67,NULL),(397,'2026-02-06 13:24:11.795748',6,'fear',24.1283,67,NULL),(398,'2026-02-06 13:24:11.796831',6,'disgust',0,67,NULL),(399,'2026-02-06 13:24:11.798055',6,'neutral',34.1009,67,NULL),(10000,'2026-02-06 16:12:43.000000',14,'happy',29.8,68,NULL),(10001,'2026-02-06 16:14:06.000000',14,'sad',0.8,68,NULL),(10002,'2026-02-06 16:14:07.000000',14,'angry',15.8,68,NULL),(10003,'2026-02-06 16:14:08.000000',14,'surprise',10.8,68,NULL),(10004,'2026-02-06 16:14:08.000000',14,'fear',1.8,68,NULL),(10005,'2026-02-06 16:14:09.000000',14,'disgust',1.2,68,NULL),(10006,'2026-02-06 16:14:09.000000',14,'neutral',39.8,68,NULL),(10007,'2026-02-06 16:47:09.000000',15,'happy',15.5,68,NULL),(10008,'2026-02-06 16:47:09.000000',15,'sad',5.2,68,NULL),(10009,'2026-02-06 16:47:10.000000',15,'angry',2.1,68,NULL),(10010,'2026-02-06 16:47:10.000000',15,'surprise',8.4,68,NULL),(10011,'2026-02-06 16:47:10.000000',15,'fear',1.3,68,NULL),(10012,'2026-02-06 16:47:10.000000',15,'disgust',0.5,68,NULL),(10013,'2026-02-06 16:47:10.000000',15,'neutral',67,68,NULL),(10014,'2026-02-06 16:47:10.000000',16,'happy',40,68,NULL),(10015,'2026-02-06 16:47:10.000000',16,'sad',2.5,68,NULL),(10016,'2026-02-06 16:47:10.000000',16,'angry',1,68,NULL),(10017,'2026-02-06 16:47:10.000000',16,'surprise',12,68,NULL),(10018,'2026-02-06 16:47:10.000000',16,'fear',0.5,68,NULL),(10019,'2026-02-06 16:47:10.000000',16,'disgust',0,68,NULL),(10020,'2026-02-06 16:47:10.000000',16,'neutral',44,68,NULL),(10021,'2026-02-06 16:47:10.000000',17,'happy',5,68,NULL),(10022,'2026-02-06 16:47:10.000000',17,'sad',15.8,68,NULL),(10023,'2026-02-06 16:47:10.000000',17,'angry',10.2,68,NULL),(10024,'2026-02-06 16:47:10.000000',17,'surprise',4,68,NULL),(10025,'2026-02-06 16:47:10.000000',17,'fear',8.5,68,NULL),(10026,'2026-02-06 16:47:10.000000',17,'disgust',6.5,68,NULL),(10027,'2026-02-06 16:47:10.000000',17,'neutral',50,68,NULL),(10028,'2026-02-06 16:47:10.000000',18,'happy',22.1,68,NULL),(10029,'2026-02-06 16:47:10.000000',18,'sad',3.3,68,NULL),(10030,'2026-02-06 16:47:10.000000',18,'angry',0.7,68,NULL),(10031,'2026-02-06 16:47:10.000000',18,'surprise',15.9,68,NULL),(10032,'2026-02-06 16:47:10.000000',18,'fear',1.2,68,NULL),(10033,'2026-02-06 16:47:10.000000',18,'disgust',0.8,68,NULL),(10034,'2026-02-06 16:47:10.000000',18,'neutral',56,68,NULL),(10035,'2026-02-07 16:14:38.558360',6,'happy',0,84,NULL),(10036,'2026-02-07 16:14:38.559806',6,'sad',0,84,NULL),(10037,'2026-02-07 16:14:38.560863',6,'angry',0,84,NULL),(10038,'2026-02-07 16:14:38.561886',6,'surprise',0,84,NULL),(10039,'2026-02-07 16:14:38.563005',6,'fear',0,84,NULL),(10040,'2026-02-07 16:14:38.564311',6,'disgust',0,84,NULL),(10041,'2026-02-07 16:14:38.565331',6,'neutral',0,84,NULL),(10042,'2026-02-08 19:43:33.168068',14,'happy',1.57095,108,NULL),(10043,'2026-02-08 19:43:33.169318',14,'sad',2.01587,108,NULL),(10044,'2026-02-08 19:43:33.170406',14,'angry',1.19968,108,NULL),(10045,'2026-02-08 19:43:33.171411',14,'surprise',2.58683,108,NULL),(10046,'2026-02-08 19:43:33.172385',14,'fear',14.621,108,NULL),(10047,'2026-02-08 19:43:33.173332',14,'disgust',0,108,NULL),(10048,'2026-02-08 19:43:33.174252',14,'neutral',0.0152381,108,NULL),(10049,'2026-02-08 19:43:33.175490',15,'happy',1.57095,108,NULL),(10050,'2026-02-08 19:43:33.176462',15,'sad',2.01587,108,NULL),(10051,'2026-02-08 19:43:33.177539',15,'angry',1.19968,108,NULL),(10052,'2026-02-08 19:43:33.178660',15,'surprise',2.58683,108,NULL),(10053,'2026-02-08 19:43:33.179680',15,'fear',14.621,108,NULL),(10054,'2026-02-08 19:43:33.180652',15,'disgust',0,108,NULL),(10055,'2026-02-08 19:43:33.181667',15,'neutral',0.0152381,108,NULL),(10056,'2026-02-08 20:34:00.997654',16,'happy',0,111,NULL),(10057,'2026-02-08 20:34:00.999087',16,'sad',0.000232558,111,NULL),(10058,'2026-02-08 20:34:01.000191',16,'angry',0,111,NULL),(10059,'2026-02-08 20:34:01.001329',16,'surprise',0,111,NULL),(10060,'2026-02-08 20:34:01.002327',16,'fear',2.32535,111,NULL),(10061,'2026-02-08 20:34:01.003274',16,'disgust',0,111,NULL),(10062,'2026-02-08 20:34:01.004137',16,'neutral',0,111,NULL),(10063,'2026-02-08 20:34:01.005108',15,'happy',0,111,NULL),(10064,'2026-02-08 20:34:01.006064',15,'sad',0.000232558,111,NULL),(10065,'2026-02-08 20:34:01.006938',15,'angry',0,111,NULL),(10066,'2026-02-08 20:34:01.007800',15,'surprise',0,111,NULL),(10067,'2026-02-08 20:34:01.008696',15,'fear',2.32535,111,NULL),(10068,'2026-02-08 20:34:01.009512',15,'disgust',0,111,NULL),(10069,'2026-02-08 20:34:01.010319',15,'neutral',0,111,NULL),(10070,'2026-02-08 20:37:56.104488',15,'happy',0,113,NULL),(10071,'2026-02-08 20:37:56.105477',15,'sad',0,113,NULL),(10072,'2026-02-08 20:37:56.106368',15,'angry',0,113,NULL),(10073,'2026-02-08 20:37:56.107334',15,'surprise',0,113,NULL),(10074,'2026-02-08 20:37:56.108291',15,'fear',0,113,NULL),(10075,'2026-02-08 20:37:56.109245',15,'disgust',0,113,NULL),(10076,'2026-02-08 20:37:56.110185',15,'neutral',0,113,NULL),(10077,'2026-02-08 20:39:47.032767',15,'happy',0,114,NULL),(10078,'2026-02-08 20:39:47.033841',15,'sad',0,114,NULL),(10079,'2026-02-08 20:39:47.034778',15,'angry',0,114,NULL),(10080,'2026-02-08 20:39:47.035627',15,'surprise',0,114,NULL),(10081,'2026-02-08 20:39:47.036499',15,'fear',0,114,NULL),(10082,'2026-02-08 20:39:47.037442',15,'disgust',0,114,NULL),(10083,'2026-02-08 20:39:47.038438',15,'neutral',0,114,NULL),(10084,'2026-02-08 21:18:50.603263',17,'happy',7.40622,117,NULL),(10085,'2026-02-08 21:18:50.604351',17,'sad',14.7274,117,NULL),(10086,'2026-02-08 21:18:50.605664',17,'angry',10.7699,117,NULL),(10087,'2026-02-08 21:18:50.607468',17,'surprise',2.72771,117,NULL),(10088,'2026-02-08 21:18:50.609389',17,'fear',21.8361,117,NULL),(10089,'2026-02-08 21:18:50.610906',17,'disgust',0,117,NULL),(10090,'2026-02-08 21:18:50.612406',17,'neutral',27.4844,117,NULL),(10091,'2026-02-08 21:18:50.613618',14,'happy',0.512587,117,NULL),(10092,'2026-02-08 21:18:50.615937',14,'sad',0.211443,117,NULL),(10093,'2026-02-08 21:18:50.617180',14,'angry',4.58711,117,NULL),(10094,'2026-02-08 21:18:50.618353',14,'surprise',0.00830846,117,NULL),(10095,'2026-02-08 21:18:50.619583',14,'fear',5.06244,117,NULL),(10096,'2026-02-08 21:18:50.620862',14,'disgust',0.452338,117,NULL),(10097,'2026-02-08 21:18:50.622149',14,'neutral',0.00900497,117,NULL),(10098,'2026-02-08 21:18:50.623327',15,'happy',3.9594,117,NULL),(10099,'2026-02-08 21:18:50.624425',15,'sad',7.46941,117,NULL),(10100,'2026-02-08 21:18:50.625613',15,'angry',7.67848,117,NULL),(10101,'2026-02-08 21:18:50.626586',15,'surprise',1.36801,117,NULL),(10102,'2026-02-08 21:18:50.627853',15,'fear',13.4493,117,NULL),(10103,'2026-02-08 21:18:50.629317',15,'disgust',0.226169,117,NULL),(10104,'2026-02-08 21:18:50.630766',15,'neutral',13.7467,117,NULL);
/*!40000 ALTER TABLE `emotion_graph` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exit_quiz`
--

DROP TABLE IF EXISTS `exit_quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exit_quiz` (
  `exit_quiz_id` bigint NOT NULL AUTO_INCREMENT,
  `answer_number` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `question` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` tinyint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`exit_quiz_id`),
  KEY `FK1w413pd8vt354t6lju6yb40we` (`session_id`),
  CONSTRAINT `FK1w413pd8vt354t6lju6yb40we` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`),
  CONSTRAINT `exit_quiz_chk_1` CHECK ((`type` between 0 and 1))
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exit_quiz`
--

LOCK TABLES `exit_quiz` WRITE;
/*!40000 ALTER TABLE `exit_quiz` DISABLE KEYS */;
INSERT INTO `exit_quiz` VALUES (1,2,'2026-02-04 14:53:17.874833','다음 중 스프링 시큐리티의 FilterChain에 대한 설명으로 옳은 것은?',NULL,'2026-02-04 14:53:17.874848',3),(2,1,'2026-02-05 17:23:23.759766','질문1',0,'2026-02-05 17:23:23.759783',36),(3,1,'2026-02-06 13:05:44.158733','ㅇㅇ',0,'2026-02-06 13:05:44.159003',60),(4,1,'2026-02-06 15:45:16.044331','70번 세션 퀴즈 생성 테스트',0,'2026-02-06 15:45:16.044349',70),(5,1,'2026-02-06 16:00:00.455741','76번 세션 퇴쉴퀴즈 테스트트트트트',0,'2026-02-06 16:00:00.455760',76),(6,1,'2026-02-06 16:04:15.279961','77퇴퀴',0,'2026-02-06 16:04:15.280005',77),(7,1,'2026-02-06 16:06:53.808524','7878',0,'2026-02-06 16:06:53.808566',78),(8,1,'2026-02-06 16:10:22.502107','79번',0,'2026-02-06 16:10:22.502126',79),(9,1,'2026-02-06 16:45:36.737749','ㅁㄴㅇㄹㄷㄻ',0,'2026-02-06 16:45:36.737767',80),(10,1,'2026-02-06 17:07:07.000000','SSAFY 15기 입과과정에서 인터뷰시 확인하는 것은? ',0,'2026-02-06 17:07:07.000000',68),(11,2,'2026-02-06 17:08:05.000000','SSAFY 15기 입과과정에서 입과 가능한 생년월일은? ',0,'2026-02-06 17:08:05.000000',68),(12,1,'2026-02-06 19:21:13.387498','질문입니다',0,'2026-02-06 19:21:13.387521',82),(13,1,'2026-02-08 13:22:47.558610','퇴실퀴즈 테스으',0,'2026-02-08 13:22:47.558662',89),(14,1,'2026-02-08 16:06:45.398729','asdf',0,'2026-02-08 16:06:45.398748',91),(15,1,'2026-02-08 16:08:12.396087','qwerq',0,'2026-02-08 16:08:12.396106',92),(16,1,'2026-02-08 16:08:12.401682','qwer',0,'2026-02-08 16:08:12.401693',92),(17,1,'2026-02-08 16:16:18.052603','safd',0,'2026-02-08 16:16:18.052620',92),(18,1,'2026-02-08 16:18:20.486345','reqw',0,'2026-02-08 16:18:20.486366',93),(19,1,'2026-02-08 16:19:32.529352','wqer',0,'2026-02-08 16:19:32.529369',94),(20,1,'2026-02-08 16:59:14.192667','ㄴㅇㅁㄹ',0,'2026-02-08 16:59:14.192721',95),(21,1,'2026-02-08 18:06:02.700716','강남스타일',0,'2026-02-08 18:06:02.700734',98),(22,1,'2026-02-08 19:30:29.813789','123',0,'2026-02-08 19:30:29.813806',107),(23,1,'2026-02-08 21:18:47.576361','123',0,'2026-02-08 21:18:47.576385',117);
/*!40000 ALTER TABLE `exit_quiz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exit_quiz_answer`
--

DROP TABLE IF EXISTS `exit_quiz_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exit_quiz_answer` (
  `exit_quiz_answer_id` bigint NOT NULL AUTO_INCREMENT,
  `answer` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `number` int NOT NULL,
  `exit_quiz_id` bigint NOT NULL,
  PRIMARY KEY (`exit_quiz_answer_id`),
  KEY `FKtldmynkrtbl3p7hdk6rrgtruj` (`exit_quiz_id`),
  CONSTRAINT `FKtldmynkrtbl3p7hdk6rrgtruj` FOREIGN KEY (`exit_quiz_id`) REFERENCES `exit_quiz` (`exit_quiz_id`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exit_quiz_answer`
--

LOCK TABLES `exit_quiz_answer` WRITE;
/*!40000 ALTER TABLE `exit_quiz_answer` DISABLE KEYS */;
INSERT INTO `exit_quiz_answer` VALUES (1,'Servlet Filter보다 먼저 실행된다.',1,1),(2,'여러 개의 필터가 체인 형태로 연결되어 동작한다.',2,1),(3,'Bean으로 등록할 수 없다.',3,1),(4,'정답 없음',4,1),(5,'선택지1',1,2),(6,'선택지2',2,2),(7,'선택지3',3,2),(8,'선택지4',4,2),(9,'11',1,3),(10,'22',2,3),(11,'33',3,3),(12,'44',4,3),(13,'70번 세션 퀴즈 생성 테스트 <<<<<<<<<<<<<<<',1,4),(14,'70번 세션 퀴즈 생성 테스트',2,4),(15,'70번 세션 퀴즈 생성 테스트',3,4),(16,'70번 세션 퀴즈 생성 테스트',4,4),(17,'76번 세션 퇴쉴퀴즈 테스트트트트트1',1,5),(18,'76번 세션 퇴쉴퀴즈 테스트트트트트',2,5),(19,'76번 세션 퇴쉴퀴즈 테스트트트트트',3,5),(20,'76번 세션 퇴쉴퀴즈 테스트트트트트',4,5),(21,'77퇴퀴 선택지1',1,6),(22,'77퇴퀴 선택지2',2,6),(23,'77퇴퀴 선택지3',3,6),(24,'77퇴퀴 선택지4',4,6),(25,'78781',1,7),(26,'78782',2,7),(27,'78783',3,7),(28,'78784',4,7),(29,'79번1',1,8),(30,'79번2',2,8),(31,'79번3',3,8),(32,'79번4',4,8),(33,'ㅈㄹㅇㅁㄴ',1,9),(34,'ㅁㄹㄴㅇ',2,9),(35,'ㅇㅁㄴㄹ',3,9),(36,'ㄻㅇㄴ',4,9),(38,'평소 IT에 대한 관심도와 개발자로서 필요한 사고력 및 추론 능력',1,10),(39,'뛰어난 신체 능력',2,10),(40,'다른 행성으로 워프할 수 있는 순간이동 능력',3,10),(41,'팀에 물적 지원이 가능한 재산력',4,10),(46,' 95년 1월 1일',1,11),(47,' 96년 1월 1일',2,11),(48,'06년 1월 1일',3,11),(49,'07년 1월 1일',4,11),(50,'답변1',1,12),(51,'답변2',2,12),(52,'답변3',3,12),(53,'답변4',4,12),(54,'퇴실퀴즈 테스으',1,13),(55,'퇴실퀴즈 테스으',2,13),(56,'퇴실퀴즈 테스으',3,13),(57,'퇴실퀴즈 테스으',4,13),(58,'dasf',1,14),(59,'asdf',2,14),(60,'asdf',3,14),(61,'asdf',4,14),(62,'werqewr',1,15),(63,'qewrq',2,15),(64,'werqew',3,15),(65,'r',4,15),(66,'qwer',1,16),(67,'qwer',2,16),(68,'qewr',3,16),(69,'qwer',4,16),(70,'safd',1,17),(71,'asfd',2,17),(72,'sdaf',3,17),(73,'fads',4,17),(74,'eqrw',1,18),(75,'wqer',2,18),(76,'wqre',3,18),(77,'erwq',4,18),(78,'qwer',1,19),(79,'ewrq',2,19),(80,'qewrqewr',3,19),(81,'qwerqwer',4,19),(82,'ㅁㄴㅇㄹ',1,20),(83,'ㅁㄴㅇㄹ',2,20),(84,'ㅁㄴㅇㄹ',3,20),(85,'ㅁㄴㅇㄹ',4,20),(86,'1',1,21),(87,'2',2,21),(88,'4',3,21),(89,'3',4,21),(90,'123',1,22),(91,'1',2,22),(92,'1',3,22),(93,'1',4,22),(94,'123',1,23),(95,'123',2,23),(96,'123',3,23),(97,'123',4,23);
/*!40000 ALTER TABLE `exit_quiz_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exit_quiz_result`
--

DROP TABLE IF EXISTS `exit_quiz_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exit_quiz_result` (
  `exit_quiz_result_id` bigint NOT NULL AUTO_INCREMENT,
  `result` bit(1) NOT NULL,
  `result_number` int DEFAULT NULL,
  `exit_quiz_id` bigint NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`exit_quiz_result_id`),
  KEY `FKnmabak6t7ul1bt2pw6pdx6lhn` (`exit_quiz_id`),
  KEY `FKlf2j53ap1gq0n1ofhvta9p888` (`workspace_member_id`),
  CONSTRAINT `FKlf2j53ap1gq0n1ofhvta9p888` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`),
  CONSTRAINT `FKnmabak6t7ul1bt2pw6pdx6lhn` FOREIGN KEY (`exit_quiz_id`) REFERENCES `exit_quiz` (`exit_quiz_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exit_quiz_result`
--

LOCK TABLES `exit_quiz_result` WRITE;
/*!40000 ALTER TABLE `exit_quiz_result` DISABLE KEYS */;
INSERT INTO `exit_quiz_result` VALUES (1,_binary '',1,8,3),(2,_binary '',1,9,3),(3,_binary '',1,13,3),(4,_binary '',1,18,3),(5,_binary '',1,19,3),(6,_binary '',1,20,3),(7,_binary '\0',2,21,14),(8,_binary '',1,22,16),(9,_binary '',1,23,15),(10,_binary '',1,23,14);
/*!40000 ALTER TABLE `exit_quiz_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invite_code`
--

DROP TABLE IF EXISTS `invite_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invite_code` (
  `invitecode_id` bigint NOT NULL AUTO_INCREMENT,
  `code_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expired_at` datetime(6) DEFAULT NULL,
  `identify_value` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `workspace_id` bigint NOT NULL,
  PRIMARY KEY (`invitecode_id`),
  KEY `FK5uoddy3t535ufvuaea1yxhr8d` (`workspace_id`),
  CONSTRAINT `FK5uoddy3t535ufvuaea1yxhr8d` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`workspace_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invite_code`
--

LOCK TABLES `invite_code` WRITE;
/*!40000 ALTER TABLE `invite_code` DISABLE KEYS */;
INSERT INTO `invite_code` VALUES (1,'test','2026-02-04 14:43:33.827759','2026-02-19 14:59:59.999000','vzNINSol4D4Z4Qnlj5sZ33z2HRFkRCrZ31VxH3m8Gif8WjEZ4K',1),(2,'test2','2026-02-04 14:43:49.503431','2026-03-04 14:59:59.999000','FOpCJvUDlj3HlQbYuIyIJN6APb3AAZZjbbXRk0VVitKVzKgTn2',1),(6,'3333','2026-02-04 19:57:40.302868','2026-02-04 14:59:59.999000','Ji9d2TLgO2Ac6CLjyQ6R1KhpgojfdxK0LXXENsFprPRl6Gv3sC',1),(8,'fdfdfd','2026-02-04 20:01:20.709480','2026-02-18 14:59:59.999000','XL81QeAeqPzFFO8PQ34NMImhwr5hm27wUpnBzM5WjuiRIGmQq6',1),(10,'test','2026-02-04 20:07:05.383205','2026-02-27 14:59:59.999000','CqF7KdE1kss4ObWGobXn4npQSLLrmNxBdkMw21F9Ow8K4jiRFs',2),(16,'2026 싸피 15기 모집','2026-02-06 12:56:55.168366','2026-02-13 14:59:59.999000','jltNcbflz1ZjQ9YYc1aNQAs291gBO70wDiqKZjkRaaFXw1ZPxo',6),(17,'2025 싸피 14기 모집','2026-02-06 13:23:23.314250','2026-02-27 14:59:59.999000','pgEp2Mz3kDiq0r4Cdg6kQeYahdZNP5imhDZhYz0njhSqnulSfs',6),(18,'승인반려테스트','2026-02-08 01:02:06.575819','2026-02-19 14:59:59.999000','qb9XEPxRioNRMb5BusDkKPuFsM5inZWzUvav2w9p0amKiHlNEG',10),(21,'ssafy_14기_신입생_1','2026-02-08 19:53:14.790746','2026-07-23 14:59:59.999000','FMcPNGGOu3hkjHxi5EDGG6n792QrTGJhMNlwiIsrmlMuF5tBWQ',7),(22,'싸피_15기','2026-02-08 21:02:08.775363','2026-02-27 14:59:59.999000','SFb5Ijmyw4P0f99uPHEh8EFRHayiy77fLKDm29LLtAy3on6eaS',7),(23,'SSAFY_15기_1','2026-02-08 21:12:37.904921','2026-02-13 14:59:59.999000','PZlF8YFTwi7GkDG3XbuwbHYm3xlJ9wc5QpHwaTYfgg4YahFSQ6',13);
/*!40000 ALTER TABLE `invite_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leaner_analysis`
--

DROP TABLE IF EXISTS `leaner_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leaner_analysis` (
  `session_id` bigint NOT NULL,
  `avg_attendance` float NOT NULL,
  `avg_concentrate` float NOT NULL,
  `avg_quiz_correct` float NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `total_analysis_time` int NOT NULL,
  `total_learner` int NOT NULL,
  `pause_time` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`session_id`),
  CONSTRAINT `FKchm2bms9kao679w31n3j7ynui` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leaner_analysis`
--

LOCK TABLES `leaner_analysis` WRITE;
/*!40000 ALTER TABLE `leaner_analysis` DISABLE KEYS */;
INSERT INTO `leaner_analysis` VALUES (1,11.11,22.22,33.33,'2026-02-04 15:02:25.000000',100,10,NULL),(13,0,0,0,'2026-02-05 14:52:11.056422',5,0,'[]'),(17,0,0,0,'2026-02-05 13:42:06.064766',37,1,NULL),(26,0,0,0,'2026-02-05 15:17:51.861705',138,0,'[]'),(29,0,0,0,'2026-02-05 15:29:58.628039',5,0,'[{\"startTime\":3,\"endTime\":6},{\"startTime\":6,\"endTime\":9},{\"startTime\":9,\"endTime\":11}]'),(33,0,0,0,'2026-02-05 16:46:20.989326',156,1,'[]'),(34,0,0,0,'2026-02-05 17:14:42.938843',3,0,'[]'),(36,0,0,0,'2026-02-05 17:16:32.413946',34,1,'[]'),(37,0,0,0,'2026-02-05 17:28:24.655093',166,0,'[{\"startTime\":164,\"endTime\":166},{\"startTime\":166,\"endTime\":168},{\"startTime\":167,\"endTime\":169},{\"startTime\":169,\"endTime\":171},{\"startTime\":171,\"endTime\":172}]'),(38,0,0,0,'2026-02-06 08:54:33.302881',127,1,'[]'),(39,0,0,0,'2026-02-06 08:58:42.272769',123,0,'[]'),(40,100,2933.57,0,'2026-02-06 09:03:05.507156',158,1,'[]'),(43,0,0,0,'2026-02-06 09:31:06.727187',1054,0,'[]'),(44,50,842.636,0,'2026-02-06 09:34:07.816221',127,2,'[]'),(45,50,757.031,0,'2026-02-06 09:39:24.194504',175,2,'[]'),(46,50,1491.23,0,'2026-02-06 09:43:07.516161',163,2,'[]'),(47,100,1768.25,0,'2026-02-06 10:01:06.587231',898,1,'[{\"startTime\":11,\"endTime\":13},{\"startTime\":12,\"endTime\":13}]'),(48,100,2930.73,0,'2026-02-06 10:35:42.636057',514,1,'[]'),(51,0,0,0,'2026-02-06 10:51:55.837625',2,1,'[]'),(53,100,2015.8,0,'2026-02-06 11:08:28.121199',19,3,'[]'),(55,100,2039.47,0,'2026-02-06 12:09:57.268488',93,1,'[]'),(57,100,2390.96,0,'2026-02-06 12:24:00.795791',315,1,'[{\"startTime\":20,\"endTime\":22},{\"startTime\":231,\"endTime\":232}]'),(58,100,5479.14,0,'2026-02-06 12:48:04.117641',1279,1,'[]'),(59,50,1713.86,0,'2026-02-06 12:57:18.714342',404,2,'[]'),(60,50,1290.21,0,'2026-02-06 13:05:56.334886',383,2,'[]'),(61,0,3627.08,0,'2026-02-06 13:01:57.716017',131,1,'[]'),(62,100,3457.08,0,'2026-02-06 13:05:57.726096',161,1,'[]'),(63,100,4550,0,'2026-02-06 13:08:11.201351',76,1,'[]'),(64,0,0,0,'2026-02-06 13:11:43.180418',179,0,'[]'),(65,0,2730.56,0,'2026-02-06 13:15:49.656706',165,1,'[]'),(66,0,0,0,'2026-02-06 13:21:25.865539',5,0,'[]'),(67,100,2758.85,0,'2026-02-06 13:24:11.771584',54,1,'[]'),(68,92,44.6,72,'2026-02-06 14:39:58.740470',32,0,'[{\"startTime\":3300,\"endTime\":3600}]'),(75,95,34.1,90,'2026-02-06 14:55:28.886730',86,0,'[]'),(82,88,31.9,90,'2026-02-08 15:46:42.000000',60,4,'[]'),(83,100,32.7,92,'2026-02-08 12:05:57.000000',100,4,'[]'),(84,0,0,0,'2026-02-07 16:14:38.554734',77,3,'[{\"startTime\":6,\"endTime\":8}]'),(85,95,38,100,'2026-02-08 12:05:57.000000',100,4,'[]'),(108,100,2451.79,0,'2026-02-08 19:43:33.140127',63,1,'[]'),(111,100,1000,0,'2026-02-08 20:34:00.991916',129,0,'[]'),(113,0,0,0,'2026-02-08 20:37:56.072884',105,0,'[]'),(114,0,0,0,'2026-02-08 20:39:47.018633',54,1,'[]'),(117,100,3376.12,0,'2026-02-08 21:18:50.596175',201,2,'[]');
/*!40000 ALTER TABLE `leaner_analysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `age` bigint DEFAULT NULL,
  `auth_provider` enum('GOOGLE','KAKAO','LOCAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `email` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('ETC','FEMALE','MALE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(63) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(13) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'집주소',99,'LOCAL','2026-02-04 14:05:35.924779',NULL,'asdf@asdf','FEMALE','소서영','$2a$10$EswArrRB5/j4YltO2HOdc.5AX50RlzhMnM28S0572Pw52wUkRTUoi',NULL,NULL,NULL,'USER',NULL),(2,'',NULL,'LOCAL','2026-02-04 14:06:36.404458',NULL,'saffy4@a.com','MALE','EZ2ON 유저','$2a$10$qATL9s9SsZUo645p5FdjyuLHEZDcYGypJZtUopShCg66csfkDShJu',NULL,NULL,NULL,'USER',NULL),(3,'주소예시',11,'LOCAL','2026-02-04 14:06:53.865292',NULL,'Test1@test.com','MALE','테스트 유저','$2a$10$WMexSyAX/fT8QYOgivKRleKT4TBCO7c4AmkB2TCWpc9s0Pg9jvbIa',NULL,NULL,NULL,'ADMIN',NULL),(4,'집주소',99,'LOCAL','2026-02-04 14:27:47.960600',NULL,'saffy@a.com','MALE','구선생생생','$2a$10$F.PUSSYoTi9eQm8L1Q/CX.zYW6yJ/IF4ymGC0UPim0Uvx1rcnuUfS',NULL,NULL,NULL,'ADMIN',NULL),(5,'집주소',99,'LOCAL','2026-02-04 14:33:59.105506',NULL,'admin@asdf','FEMALE','ASDF','$2a$10$aQ/TEO9P329bAh1.5ZSOUOYerYzsAcwJl.kJIiXSUX6Aex7IeTrIO',NULL,NULL,NULL,'ADMIN',NULL),(6,'주소주소',11,'LOCAL','2026-02-04 14:35:23.411577',NULL,'trainer@trainer.com','MALE','김강사','$2a$10$yhuhftiai6ECZUNgvNOP2.9zdF0lK87WKM3UCPnl58OK9vR3Nc/3S',NULL,NULL,NULL,'USER',NULL),(7,'주소예시',123,'LOCAL','2026-02-04 14:37:07.573569',NULL,'learner@learner.com','MALE','김학생','$2a$10$drgZB2BLdcwjjSI4tNEfi.D2OQqh1aUbUlx/BvZ25cLm34.uBAaAG',NULL,NULL,NULL,'USER',NULL),(8,'역삼동',365,'LOCAL','2026-02-05 09:22:44.638672',NULL,'admintest@a.com','MALE','관리자테스트용','$2a$10$GjuNDCOsX0lAXwUVrb/7ROBtw3SLQOoYpOgmFD9smnPiMc3hgk.7i',NULL,NULL,NULL,'USER',NULL),(9,'역삼동',222,'LOCAL','2026-02-05 14:09:45.614906',NULL,'learnertest@test.com','MALE','테스트합니다.','$2a$10$3os/dz1TFC9J7PuQlDYlc.j5p8obMpXV4MLHFMl5nfAoJ5gUp8c7e',NULL,NULL,NULL,'USER',NULL),(10,'11',11,'LOCAL','2026-02-05 14:25:28.023116',NULL,'111@1.com','MALE','111','$2a$10$GRZGmyG9jll0IiWIfQNTC.vkVbF2L4wJQqql8gGkuQF3.nQ.qA/TK',NULL,NULL,NULL,'USER',NULL),(11,'12',12,'LOCAL','2026-02-05 14:39:36.222433',NULL,'222@1.com','MALE','222','$2a$10$xHCf8teCTLRsRcYE/OjaZOoYxETUy7powyeniA3facIkhkSTXV1be',NULL,NULL,NULL,'USER',NULL),(12,'111',111,'LOCAL','2026-02-06 08:55:34.615704',NULL,'test@test.com','MALE','테스트','$2a$10$lbs3MoQlsp1C2QNDKHFW2.zNFJ9ILNAvV8KTpKvtcrzlUghL6Wg1C',NULL,NULL,NULL,'USER',NULL),(13,'경기도 수원시 발달로 12길, 32',40,'LOCAL','2026-02-06 09:25:33.669313',NULL,'admin@play.com','MALE','SSAFY 관리자','$2a$10$8iT4imakyqBiPMEHxb7nb.Ro3gtAUUupPTrSIs/D7SLQsSiR1NytO',NULL,NULL,'https://d285ryfcbcw5yb.cloudfront.net/admin_profile.webp','ADMIN',NULL),(14,'경기도 고양시',27,'LOCAL','2026-02-06 09:35:32.842452',NULL,'ymj@play.com','MALE','유민재','$2a$10$sGh5pna8OzGu0QGH6ywcC.4jlow2ohYwRh/wzo1m24ywPno0WF8zu',NULL,NULL,'https://d285ryfcbcw5yb.cloudfront.net/ymj_profile.jpg','USER',NULL),(15,'서울 관악구',27,'LOCAL','2026-02-06 09:36:43.450149',NULL,'lsy@play.com','MALE','이서영','$2a$10$r0WICeD9wWuew9qopmgnT.7o6SJjVEcJouDPQPwQ5U/ZVq1OEy.Zq',NULL,NULL,'https://d285ryfcbcw5yb.cloudfront.net/lsy_profile.png','USER',NULL),(16,'경기도 ',29,'LOCAL','2026-02-06 09:50:21.951498',NULL,'sks@play.com','MALE','서경수','$2a$10$JD7TRaIrixOJYWSdWNkO/uVWgIFcmu4BQl/N86B3gJzB4aciFfMkK',NULL,NULL,'https://d285ryfcbcw5yb.cloudfront.net/sks_profile.webp','USER',NULL),(17,'경기도 의정부시',30,'LOCAL','2026-02-06 09:50:49.302830',NULL,'jsh@play.com','MALE','정세호','$2a$10$oDFzvTwrhCVpFKipbyfZbO7Rjg.ikGR8MKPf8KsiLhAXr/JFrpHiu',NULL,NULL,'https://d285ryfcbcw5yb.cloudfront.net/jsh_profile.webp','USER',NULL),(18,'경기도 분당구',27,'LOCAL','2026-02-06 09:51:25.515282',NULL,'ksy@play.com','FEMALE','김소연','$2a$10$MvXqzxQ2OVw89F7F/1ioYuHigTmyuM2ZaqHnWrXVPaD0XF5bnBqyi',NULL,NULL,'https://d285ryfcbcw5yb.cloudfront.net/ksy_profile.webp','USER',NULL),(19,'12',12,'LOCAL','2026-02-06 10:59:53.291628',NULL,'djmax@djmax','MALE','DJMAX유저','$2a$10$gsoJ8R9Ooc70hr0PbwtGN.Gtq42zzQPRbHzn5Nw2TqLebWnMgHLLC',NULL,NULL,NULL,'USER',NULL),(20,'1232312',123,'LOCAL','2026-02-06 12:53:02.836566',NULL,'333@3.com','MALE','333','$2a$10$RfG9kuUjUUFX63uQ6cG9Led1RXoyUePNOC.OH/tT2jMqHtXdDCZcO',NULL,NULL,NULL,'USER',NULL),(21,'123',123,'LOCAL','2026-02-06 17:24:44.938416',NULL,'444@4.com','MALE','testlearn','$2a$10$JjRzJaYSDTyZ/ep.gR4biuLl3ElSGrfwovu7vZhnQpyvzuJJWqniK',NULL,NULL,NULL,'USER',NULL),(22,'123',123,'LOCAL','2026-02-07 14:58:19.688556',NULL,'testsy@test.com','MALE','test','$2a$10$dVCtpZ6/sR9WN8I6M9XIDOKr9lJBmcOppgy8F9Z.gQuc8zx4NbbGK',NULL,NULL,NULL,'USER',NULL);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qnamessage`
--

DROP TABLE IF EXISTS `qnamessage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qnamessage` (
  `qna_message_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('ANSWER','QUESTION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `qna_room_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`qna_message_id`),
  KEY `FK2xsbo395bt93jtmujb96xfgo4` (`qna_room_id`),
  KEY `FKb3tqnr3ryw90e7rjree6gjqcb` (`workspace_member_id`),
  CONSTRAINT `FK2xsbo395bt93jtmujb96xfgo4` FOREIGN KEY (`qna_room_id`) REFERENCES `qnaroom` (`qna_room_id`),
  CONSTRAINT `FKb3tqnr3ryw90e7rjree6gjqcb` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qnamessage`
--

LOCK TABLES `qnamessage` WRITE;
/*!40000 ALTER TABLE `qnamessage` DISABLE KEYS */;
/*!40000 ALTER TABLE `qnamessage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qnaroom`
--

DROP TABLE IF EXISTS `qnaroom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qnaroom` (
  `qna_room_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`qna_room_id`),
  UNIQUE KEY `UKmp32geglwp8crqtmiqeqtjyk9` (`session_id`),
  CONSTRAINT `FKm75y7tcd5oy3eyf03jk828pdn` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qnaroom`
--

LOCK TABLES `qnaroom` WRITE;
/*!40000 ALTER TABLE `qnaroom` DISABLE KEYS */;
/*!40000 ALTER TABLE `qnaroom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_chatting_message`
--

DROP TABLE IF EXISTS `review_chatting_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_chatting_message` (
  `chatting_message_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `workspace_member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`chatting_message_id`),
  KEY `FKow57stcprxlfnuli5ybd671qa` (`room_id`),
  KEY `FKbtq4s8i73o5r2k5r9kn6wfqqg` (`workspace_member_id`),
  CONSTRAINT `FKbtq4s8i73o5r2k5r9kn6wfqqg` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`),
  CONSTRAINT `FKow57stcprxlfnuli5ybd671qa` FOREIGN KEY (`room_id`) REFERENCES `review_chatting_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_chatting_message`
--

LOCK TABLES `review_chatting_message` WRITE;
/*!40000 ALTER TABLE `review_chatting_message` DISABLE KEYS */;
INSERT INTO `review_chatting_message` VALUES (1,'2026-02-04 15:02:32.612204','123',1,1,NULL),(2,'2026-02-04 15:02:32.618047','관련 내용을 강의에서 찾을 수 없습니다.',0,1,NULL),(3,'2026-02-05 15:51:15.486074','gd',2,2,NULL),(4,'2026-02-05 15:51:15.491418','제공된 내용에는 \'gd\'에 대한 정보가 포함되어 있지 않습니다. 추가적인 질문이나 구체적인 내용을 제공해 주시면 더 도움이 될 수 있습니다.',0,2,NULL),(5,'2026-02-06 12:11:52.211747','개방 폐쇄 원칙에 대해 설명해줘',2,2,NULL),(6,'2026-02-06 12:11:52.216965','관련 내용을 강의에서 찾을 수 없습니다.',0,2,NULL),(7,'2026-02-06 12:12:17.397727','SOLID 원칙에 대해 설명해줘',2,2,NULL),(8,'2026-02-06 12:12:17.399052','관련 내용을 강의에서 찾을 수 없습니다.',0,2,NULL),(9,'2026-02-06 12:12:22.677716','안녕',2,2,NULL),(10,'2026-02-06 12:12:22.679377','관련 내용을 강의에서 찾을 수 없습니다.',0,2,NULL),(12,'2026-02-06 17:35:28.875440','하이',2,2,NULL),(13,'2026-02-06 17:35:28.877629','안녕하세요! 저는 보조 강사입니다. 무엇을 도와드릴까요? 강의 내용 관련 질문이나 일반적인 궁금증 모두 환영합니다.',0,2,NULL),(14,'2026-02-06 17:35:33.904765','안녕.',2,2,NULL),(15,'2026-02-06 17:35:33.906196','안녕하세요! 만나서 반가워요. 저는 강의 내용을 기반으로 돕는 보조 강사입니다. 무엇을 도와드릴까요? 궁금한 주제나 질문, 공부하고 싶은 내용이 있으면 말씀해 주세요.',0,2,NULL),(18,'2026-02-07 17:23:37.016701','1분 30초에 무슨 이야기 했어 ?',17,6,NULL),(19,'2026-02-07 17:23:37.050463','1분 30초에 교육생들에게 제공되는 다양한 혜택에 대해 이야기했습니다. 실전형 소프트웨어 교육과 AI 교육이 1년 동안 무료로 제공되며, 교육 기간 동안 매월 100만원의 교육지원금을 지급한다고 설명했습니다. 또한, 서울을 제외한 대전, 광주, 구미, 부울경 캠퍼스에서는 정부에서 주는 훈련장려금 30만원도 추가로 받을 수 있다고 언급했습니다.',0,6,NULL),(20,'2026-02-07 17:32:32.113888','360초에 대한 보충 설명이 가능해 ?',17,6,NULL),(21,'2026-02-07 17:32:32.116232','사피에 들어가기 위한 프로세스는 간단합니다. 10월 27일부터 15기 지원서 접수가 시작되며, 지원자는 사피닷컴 웹사이트에 접속하여 지원서를 작성하면 됩니다. 이 과정은 매우 간단하여, 빠른 지원자는 1분 이내에 접수를 완료할 수 있습니다. 지원은 PC와 모바일 모두 가능하며, 지원하는 동안 유튜브 설명을 듣고 이벤트에 참여하면 햄버거 세트를 받을 수 있는 기회도 있습니다.',0,6,NULL),(22,'2026-02-07 21:03:52.778760','영상 10분에 무슨 내용을 설명하고 있어?',14,4,NULL),(23,'2026-02-07 21:03:52.812413','영상에서는 지원자들에게 적성 진단에 대한 설명과 함께 소프트웨어 전공자와 비전공자를 위한 코딩 테스트 및 기초 논리력 테스트에 대해 안내하고 있습니다. 전공자들은 온라인 CBT 기반의 코딩 테스트를 보게 되며, 비전공자들은 사고 능력을 평가하는 논리력 테스트를 진행합니다. 또한, 문제풀이를 통해 출제 유형을 경험할 수 있는 기회도 제공된다고 설명하고 있습니다.',0,4,NULL),(24,'2026-02-08 17:20:47.242681','\'잡페어\' 프로그램의 주요 목적은 무엇인가요? 1 취업에 필요한 역량을 키우기 위해 2 학업 성적을 향상시키기 위해 3 전문 자격증을 취득하기 위해 4 인턴십 기회를 제공하기 위해',14,4,NULL),(25,'2026-02-08 17:20:47.248321','1 취업에 필요한 역량을 키우기 위해',0,4,NULL),(26,'2026-02-08 18:40:30.808551','하이',7,9,NULL),(27,'2026-02-08 18:40:30.810261','안녕하세요! 무엇을 도와드릴까요?',0,9,NULL),(28,'2026-02-08 20:09:37.071742','영상 10분에 무슨 내용을 설명하고 있어?',14,4,NULL),(29,'2026-02-08 20:09:37.073773','영상에서는 지원자들에게 적성 진단에 대한 설명과 함께 소프트웨어 전공자와 비전공자를 위한 코딩 테스트 및 기초 논리력 테스트에 대해 안내하고 있습니다. 전공자들은 온라인 CBT 기반의 코딩 테스트를 통해 역량을 평가받고, 비전공자들은 사고 능력을 확인하는 논리력 테스트를 보게 됩니다. 또한, 라이브 세션을 통해 문제풀이를 경험할 수 있는 기회도 제공된다고 설명하고 있습니다.',0,4,NULL),(30,'2026-02-08 20:09:57.047343','\'잡페어\' 프로그램의 주요 목적은 무엇인가요? 1 취업에 필요한 역량을 키우기 위해 2 여가 활동을 지원하기 위해 3 학업 성적을 향상시키기 위해 4 사회적 네트워크를 확장하기 위해',14,4,NULL),(31,'2026-02-08 20:09:57.048764','1 취업에 필요한 역량을 키우기 위해',0,4,NULL),(32,'2026-02-08 21:21:54.981102','\'잡페어\' 프로그램의 주요 목적은 무엇인가요? 1 취업에 필요한 역량을 키우기 위해 2 여가 활동을 지원하기 위해 3 학업 성적을 향상시키기 위해 4 전문 자격증을 취득하기 위해',14,4,NULL),(33,'2026-02-08 21:21:54.982498','1 취업에 필요한 역량을 키우기 위해',0,4,NULL),(34,'2026-02-08 21:22:09.059119','영상 6분에 무슨 내용을 설명하고 있어?',14,4,NULL),(35,'2026-02-08 21:22:09.060375','영상 6분에서는 사피 프로그램에 참여함으로써 교육생들이 얻을 수 있는 취업 기회, 기업 인사담당자와의 소통 기회, 수료생들과의 네트워킹 기회에 대해 설명하고 있습니다. 또한, 15기 지원서 접수 방법과 절차에 대해 간단히 안내하며, 지원이 간단하다는 점과 이벤트 추첨에 대한 정보도 포함되어 있습니다.',0,4,NULL);
/*!40000 ALTER TABLE `review_chatting_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_chatting_room`
--

DROP TABLE IF EXISTS `review_chatting_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_chatting_room` (
  `room_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `member_id` bigint DEFAULT NULL,
  `session_id` bigint NOT NULL,
  `workspace_member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `UKgc47541jowjx9cxf8ydc3wjx8` (`session_id`,`member_id`),
  KEY `FKp5v7kug4cybetahtda0ecxy38` (`workspace_member_id`),
  CONSTRAINT `FKiump1sqcf6atuv8hqjlfdp52x` FOREIGN KEY (`session_id`) REFERENCES `leaner_analysis` (`session_id`),
  CONSTRAINT `FKp5v7kug4cybetahtda0ecxy38` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_chatting_room`
--

LOCK TABLES `review_chatting_room` WRITE;
/*!40000 ALTER TABLE `review_chatting_room` DISABLE KEYS */;
INSERT INTO `review_chatting_room` VALUES (1,'2026-02-04 15:02:29.997165',1,1,NULL),(2,'2026-02-05 15:51:07.497489',2,1,NULL),(3,'2026-02-06 18:04:12.575214',18,68,NULL),(4,'2026-02-06 18:44:56.186537',14,68,NULL),(5,'2026-02-06 19:17:46.056019',18,75,NULL),(6,'2026-02-06 19:18:32.057241',17,68,NULL),(7,'2026-02-07 15:05:03.907353',15,68,NULL),(8,'2026-02-07 19:22:41.087469',22,84,NULL),(9,'2026-02-07 22:07:53.433322',7,60,NULL),(10,'2026-02-08 16:20:23.262535',7,61,NULL),(11,'2026-02-08 20:22:05.496892',16,68,NULL);
/*!40000 ALTER TABLE `review_chatting_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `script`
--

DROP TABLE IF EXISTS `script`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `script` (
  `script_id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `timeline` int NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`script_id`),
  KEY `idx_session_timeline` (`session_id`,`timeline` DESC),
  CONSTRAINT `FK8psevwha6l2mxwpi0m8h0gq3k` FOREIGN KEY (`session_id`) REFERENCES `leaner_analysis` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000071 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `script`
--

LOCK TABLES `script` WRITE;
/*!40000 ALTER TABLE `script` DISABLE KEYS */;
INSERT INTO `script` VALUES (14,'안녕하세요. 오늘 강의를 시작해 보겠습니다.',0,1),(15,'먼저 지난 시간에 배운 핵심 내용을 복습해 볼까요?',10,1),(16,'오늘의 주제는 시스템 아키텍처의 설계 원칙입니다.',20,1),(17,'우리는 왜 더 나은 구조를 고민해야 할까요?',30,1),(18,'코드의 가독성뿐만 아니라 유지보수성이 중요하기 때문입니다.',40,1),(19,'첫 번째 섹션에서는 객체 지향의 5대 원칙을 다룹니다.',50,1),(20,'SOLID 원칙이라고 들어보셨을 겁니다.',60,1),(21,'S는 단일 책임 원칙, 즉 SRP를 의미합니다.',70,1),(22,'하나의 클래스는 하나의 책임만 가져야 한다는 뜻이죠.',80,1),(23,'이것이 잘 지켜지지 않으면 코드가 스파게티처럼 꼬이게 됩니다.',90,1),(24,'다음은 O, 개방 폐쇄 원칙인 OCP입니다.',100,1),(25,'확장에는 열려 있고 수정에는 닫혀 있어야 합니다.',110,1),(26,'인터페이스를 활용하면 이 원칙을 쉽게 지킬 수 있습니다.',120,1),(27,'자, 이제 화면의 예제 코드를 함께 보겠습니다.',130,1),(28,'기존 코드를 수정하지 않고 기능을 추가하는 모습이 보이시나요?',140,1),(29,'이것이 바로 디자인 패턴의 핵심 아이디어입니다.',150,1),(30,'세 번째는 리스코프 치환 원칙, LSP입니다.',160,1),(31,'자식 클래스는 언제나 부모 클래스를 대체할 수 있어야 하죠.',170,1),(32,'네 번째는 인터페이스 분리 원칙, ISP입니다.',180,1),(33,'클라이언트는 자신이 사용하지 않는 메서드에 의존하면 안 됩니다.',190,1),(34,'범용 인터페이스 하나보다 구체적인 여러 개가 낫습니다.',200,1),(35,'마지막은 의존역전 원칙, DIP입니다.',210,1),(36,'고수준 모듈은 저수준 모듈에 의존해서는 안 됩니다.',220,1),(37,'자, 이제 구체적인 구현체보다는 추상화에 의존해 봅시다.',230,1),(38,'이 다섯 가지 원칙을 지키면 테스트가 쉬워집니다.',240,1),(39,'이제 잠시 쉬어가는 의미로 퀴즈를 하나 내볼까요?',250,1),(40,'다음 중 SRP를 가장 잘 설명한 것은 무엇일까요?',260,1),(41,'채팅창이나 댓글로 정답을 남겨주시기 바랍니다.',270,1),(42,'정답은 강의 하단 설명란에서도 확인하실 수 있습니다.',280,1),(43,'자, 다시 본론으로 돌아와서 설계 실습을 해보겠습니다.',290,1),(44,'도메인 모델을 먼저 정의하는 것이 순서입니다.',300,1),(45,'사용자의 요구사항을 분석해서 명사들을 추출해 보세요.',310,1),(46,'추출된 명사들은 클래스의 후보가 됩니다.',320,1),(47,'동사들은 해당 클래스의 메서드가 될 가능성이 높습니다.',330,1),(48,'이런 방식을 데이터 중심 설계라고 부르기도 합니다.',340,1),(49,'하지만 실제로는 책임 중심 설계가 더 권장됩니다.',350,1),(50,'객체가 무엇을 아는지보다 무엇을 하는지가 중요하니까요.',360,1),(51,'자, 370초 지점까지 왔습니다. 절반 정도 진행되었네요.',370,1),(52,'잠시 화면을 멈추고 직접 코드를 타이핑해 보세요.',380,1),(53,'눈으로 보는 것과 직접 짜보는 것은 천지차이입니다.',390,1),(54,'IDE 설정을 확인하고 컴파일 에러가 없는지 체크하세요.',400,1),(55,'56. Вы можете выбрать то, что вам нравится, так же четко, как iPhone 12 Pro XD.',0,33),(56,'',10,33),(57,'',20,33),(58,'',30,33),(59,'여러분, 안녕하십니까? 사이좋게 오셨으면 좋겠습니다. 이렇게 시작하겠습니다. 여러분, 안녕하십니까? 감사합니다.',40,33),(60,'귀찮아서 혹시 혼자 영상 찍은 적 있어요? 아니요, 많이 봤어요, 카페에서.',50,33),(61,'네, 기운 날 때마다 먹을 수 있습니다. 알겠습니다. 그래서 이제 3박 4일 동안은 안 드리겠습니다. 네, 안 드리겠습니다. 3박 4일 동안은 안 드리겠습니다.',60,33),(62,'오늘 영상은 여기까지입니다! 다음 영상에서 만나요! 안녕!',70,33),(63,'기본 요리의 필라를 실패했습니다. 오늘의 영상은 여기까지입니다. 좋아요와 구독 부탁드립니다.',80,33),(64,'',90,33),(65,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요. 감사합니다.',100,33),(66,'정국은 시크를 가지고 있는 관계를 유지할 전망이 다 만들어져 있습니다.',0,34),(67,'다시 한 줄이 남았다. 너무 강의하는 게 보기 싫어. 그러면 중앙팀, 두 개, 우리 팀 중앙팀이야. 그런데 멀리서 쳐서 머리가...',10,34),(68,'',20,34),(69,'밥도 먹고 살짝 따라왔다가 보고 싶다고.',30,34),(70,'',40,34),(71,'나쁜 사람에게 집중하다 보니 분위기가 떨어지는 것 같아요. 색이 너무 예뻐요.',50,34),(72,'SEVENTEEN TV는 구독, 좋아요, 알림 설정 부탁드립니다.',60,34),(73,'눈을 감았어. 예스. 눈을 떴어. 귀여워. 지금 눈을 감았는데, 이미 이불을 덮고 있어.',71,34),(74,'',82,34),(75,'죄송하지만, 제공된 텍스트는 이해할 수 없는 문자로 구성되어 있어 교정할 수 없습니다. 다른 텍스트를 제공해 주시면 기꺼이 도와드리겠습니다.',92,34),(76,'엔드 에너지 센스? 그게 어떤 거였죠? 강의 종료, 강의 종료는 대표가 호출했죠.',102,34),(77,'',112,34),(78,'그러면 여기까지 하겠습니다. 감사합니다.',122,34),(79,'여기서 분석 데이터가 돌아가고 있는데, 값이 나오지 않아서 아무것도 진행할 수 없네요.',132,34),(80,'지금 브랜치로 푸시하시면 제가 브랜치에 들어가도',142,34),(81,'',152,34),(82,'',162,34),(83,'',173,34),(84,'',184,34),(85,'어? 한요? 저 이거 까맣게... 우와... 까맣게... 까맣게... 까맣게...',195,34),(86,'왜요? 지금까지 저장했는데? 쉐이크가 저장돼요, 이거 맞아요?',206,34),(87,'이 영상은 유료 광고를 포함하고 있습니다.',217,34),(88,'이거 테스트했었죠? 40번까지는 테스트했었죠? 네, 왜 러시아어인데요? 오지 러시아어.',228,34),(89,'',239,34),(90,'',250,34),(91,'네, 제가 된 게 아닌데 좋아요와 구독은 아까 우리가 유튜브를 틀었던 거에서 제가 테스트할 때 소리가 들어가서 된 건가요? 대사가 좀...',261,34),(92,'이거 기억나나요? 이거 SCT랑 좋아요, 구독하고 검색하라고 있는 거예요.',272,34),(93,'지금 34번 사용하고 계신데, 33번이면 제가 세션을 만들어 드릴게요.',283,34),(94,'죽은 지 얼마 안 된 것이라 그때 그게 맞는 것 같은데, 최근에 그래도 저장되는 게 어디입니까? 안 되는 게 어디입니까? 지금까지 한 번도 안 됐어요.',294,34),(95,'미니언어시스는 러거언어시스 3,300원, 지금도 평균 빠른 것이 나오고 토타언어시스는 3,500원입니다.',305,34),(96,'아니, 그러니까 이게 집중도가 실시간 조정이 학생들에게는 되는데, 이번에 또 갑자기',315,34),(97,'선생님이 안 되는데 제가 선생님 파일을 하나도 안 건드렸거든요?',325,34),(98,'초반부에 시작하는 내레이터',335,34),(99,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요!',345,34),(100,'저 비밀번호를 영어로 적어주세요. 그런데 학생에게 지금 나이트 비밀번호가 뜨는 것은...',355,34),(101,'',365,34),(102,'튜너리 1등으로 해서 값은 학생에게 넘어오는데, 지금 선생님에게는 갑자기 안 넘어오네요.',375,34),(103,'지금 밸런스가 정말 급격하죠? 네, 그 타임라인이 맞지 않을 가능성이 매우 높습니다. 타임라인? 아, 그러면 이거 한번 세션 해보죠.',385,34),(104,'그래서 경수님 목소리는 선생님으로 들어가라고 했어요. 아, 됐다. 싱크로율이 줄었나 봐요.',0,36),(105,'아, 그래도 다행이다. 그러면 저거, 시선 보정만 해주시면 지금 기능 구형 거의...',10,36),(106,'',0,37),(107,'',10,37),(108,'이거 어떻게 찍어야 하지? 안 되는데? 포장을 찍어야 하나? 포장이 너무 어렵다. 포장은 안 찍을래.',20,37),(109,'어? 또 타임라인이 0으로 나가네. 이거 그렇습니다. 이모티콘도 타임라인이 0으로 나가네요. 아, 이거 합치다가...',30,37),(110,'그거 말씀하신 것, 그것도 문제였군요. 합소할 때 기존 포트가 사라지진 않았나요? 기존 포트와 합소 이름이 똑같은 건가요?',40,37),(111,'둘 중 하나를 선택해야 하는데 제가 잘못 눌렀어요.',50,37),(112,'',60,37),(113,'',70,37),(114,'오늘 영상은 여기까지입니다. 다음에 또 만나요. 안녕!',80,37),(115,'이번 영상은 여기까지입니다. 감사합니다. 다음 영상에서 만나요.',90,37),(116,'여러분 덕분에 오늘 영상도 재밌게 보셨다면 구독과 좋아요 부탁드립니다! 그럼 다음 영상에서 만나요!',100,37),(117,'다음 영상에서 만나요. 구독과 좋아요 부탁드립니다.',111,37),(118,'오늘 영상은 여기까지입니다! 다음 영상에서 만나요! 안녕!',122,37),(119,'',133,37),(120,'',0,38),(121,'',10,38),(122,'완벽하게 했습니다! 이 véritTV는 끝!',20,38),(123,'',30,38),(124,'리스폰트는 이제 WAV를 줄 거라고 얘기했는데, 실제로는 웹엔딩이니까 내부에서 좀 더 낫다고 생각할게요.',41,38),(125,'',0,39),(126,'',11,39),(127,'What happened? I don\'t know. I don\'t know. I don\'t know. I don\'t know. I don\'t know.',22,39),(128,'',33,39),(129,'',0,40),(130,'13일은... 그럼 이거 비싸 보인다. 비싸 보인다. 떡튀김 했죠.',10,40),(131,'',20,40),(132,'재미있게 보셨다면 구독과 좋아요 부탁드립니다.',30,40),(133,'',40,40),(134,'순바는 지금 1차로 정하지만, 어제 말씀드린 대로 옆방의 IoT를 기반으로 이기겠습니다.',50,40),(135,'시험 각성 문제 때문에 조정 목이 있어졌습니다. 그건 다 예고해 주시고, 연습부터 한번 시작하겠습니다.',60,40),(136,'',70,40),(137,'1등이요? 1등이요, 우리? 헐, 아직 안 들어왔군.',80,40),(138,'기어이 관련된 기능만 넣었고, 그 다음에 피스타, 그 다음에 피스타 서비스 하겐만 있으면 될 것 같습니다. 이제 한 표, 한 100 정도 나왔습니다.',0,43),(139,'우리 실제 서피스가 적지 않아? 실제 서피스를 많이 넣어야 핵이 괜찮아. 무조건 가야 돼.',10,43),(140,'들어가서 미미가 이 연습을 따라할 때 기분을 약간 강조하는 것들이 있거든요. 그렇게 한 번만 더 넣어보는 게 너무 재밌는 것 같아요.',20,43),(141,'시원해서 보여주는 것은 아직 그게 아닌 것 같은데요? 합치기만 안 올려주세요, 잠깐만요.',31,43),(142,'',42,43),(143,'',53,43),(144,'',64,43),(145,'엔지니어가 랩서버 역할을 하니 조작사에 영향을 미칠 것 같다.',75,43),(146,'그냥 엔지니어스 카페 파일이 나오고, 어... 글투 파일을 엔지니어스에 올리시면 되죠? 어... 일단 세팅을 해놨거든요. 다시 글 올린 다음에 엔지니어스에 다시 올리면 됩니다.',85,43),(147,'후 샥 새 오뎅을 참을 뭐 운뉴 서빙 후',96,43),(148,'',106,43),(149,'',116,43),(150,'今日の動画はここまでです。次の動画でお会いしましょう！最後までご視聴いただきありがとうございます。いつもありがとうございます。',127,43),(151,'',138,43),(152,'다시, 이완 그래, 다시 로그아웃 다시 다시 배포',149,43),(153,'Các bạn hãy đăng ký kênh LalaSchool để không bỏ lỡ những video hấp dẫn.',160,43),(154,'오늘 영상은 여기까지입니다. 재밌게 보셨다면 좋아요와 구독 부탁드립니다!',170,43),(155,'',180,43),(156,'',190,43),(157,'',200,43),(158,'',210,43),(159,'',220,43),(160,'',231,43),(161,'',242,43),(162,'',253,43),(163,'',264,43),(164,'여러분 덕분에 오늘도 맛있는 식사를 즐겼습니다. 구독과 좋아요는 저에게 큰 힘이 됩니다. 시청해 주셔서 감사합니다.',275,43),(165,'이상으로 마치겠습니다. 시청해 주셔서 감사합니다. 좋아요와 구독 부탁드립니다. 다음 영상에서 만나요.',286,43),(166,'',297,43),(167,'습관적인 소통, 화이팅! 방해 요소가 좋다.',307,43),(168,'',317,43),(169,'Якщо вам подобається їжа, не забудьте поставити мені лайк.',327,43),(170,'',337,43),(171,'',347,43),(172,'4. 한 번 더.  \n5. 한 번 더.  \n6. 한 번 더.  \n7. 한 번 더.  \n8. 한 번 더.',357,43),(173,'',368,43),(174,'',379,43),(175,'කරන්න අපි හොඳින් විය හැක.',390,43),(176,'',401,43),(177,'',412,43),(178,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요. 안녕!',423,43),(179,'소중한 세 번째 고기를 미리 데워가면서 찍고 오세요, 형님. 여긴 빨래방이 아니에요? 빨래 굽는 데 이런 거 못해봤는데...',434,43),(180,'',445,43),(181,'',456,43),(182,'',467,43),(183,'',478,43),(184,'',489,43),(185,'',500,43),(186,'',511,43),(187,'',522,43),(188,'Thank you for watching! I hope you enjoyed this video. If you did, make sure to give it a thumbs up and subscribe to my channel for more videos like this. I\'ll see you in the next video. Bye!',533,43),(189,'',544,43),(190,'화요일 너무 졸린 뷔 가사 좋으세요? TMI 가사 편집',555,43),(191,'',566,43),(192,'',577,43),(193,'이 영상은 루퐁이의 유료 광고를 포함하고 있습니다.',588,43),(194,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요.',599,43),(195,'',610,43),(196,'오늘 영상은 여기까지입니다! 다음 영상에서 만나요! 안녕!',621,43),(197,'일단 만두를 넣는다는 것을 몰랐습니다.',632,43),(198,'오늘은 여기까지입니다. 다음 영상에서 만나요. 안녕.',643,43),(199,'강남 스타일',654,43),(200,'',0,44),(201,'',10,44),(202,'죄송하지만, 제공된 텍스트는 이해할 수 있는 문장이 아닙니다. 다른 내용을 제공해 주시면 교정해 드리겠습니다.',21,44),(203,'最後までご視聴いただきありがとうございました。',32,44),(204,'그래서 차갑습니다. 간단하기 때문에 밥이랑 두부를 해먹으려고 했는데, 아, 더워요.',43,44),(205,'',53,44),(206,'다음번에는 한 가지 영상을 보며, 물어본다고 제가 말을 한 번 더 하지 않고, 이 SHE perspective에 대해 좀 더 이야기하겠습니다.',64,44),(207,'Якщо вам подобається їжа, не забудьте поставити мені лайк та підписатися на канал. Я — Qiong. До зустрічі в наступному відео!',0,45),(208,'',10,45),(209,'',21,45),(210,'',32,45),(211,'',43,45),(212,'',54,45),(213,'',65,45),(214,'Дякуємо за перегляд і до зустрічі в наступному відео!',0,46),(215,'',10,46),(216,'',21,46),(217,'',32,46),(218,'',43,46),(219,'',54,46),(220,'? 이 영상을 친구들과 소셜 미디어에 공유하세요.',65,46),(221,'Якщо вам подобається їжа, не забудьте поставити мені лайк і підписатися на канал, щоб не пропустити нові відео.',76,46),(222,'テロップ - 部屋が狭いため、ievesは衛生棚の中で寝てしまった。',0,47),(223,'죄송하지만, 해당 요청은 처리할 수 없습니다.',0,47),(224,'',10,47),(225,'',20,47),(226,'',30,47),(227,'굉장히 흥미로운 게임입니다. 우 Fantaystic',40,47),(228,'',41,47),(229,'',51,47),(230,'',62,47),(231,'',73,47),(232,'',83,47),(233,'',93,47),(234,'',104,47),(235,'',115,47),(236,'',126,47),(237,'',137,47),(238,'',148,47),(239,'재미있게 보셨다면 구독과 좋아요 부탁드립니다.',159,47),(240,'혼나가자, 6명 두고 갔으면 야, 왜 이렇게 놀랐어? 10초야? 생각하지 못한 게.',170,47),(241,'',181,47),(242,'',192,47),(243,'',203,47),(244,'',214,47),(245,'',225,47),(246,'',235,47),(247,'',246,47),(248,'',257,47),(249,'',268,47),(250,'',279,47),(251,'',290,47),(252,'',301,47),(253,'',312,47),(254,'',323,47),(255,'',334,47),(256,'',345,47),(257,'',356,47),(258,'',367,47),(259,'',378,47),(260,'',389,47),(261,'',400,47),(262,'',411,47),(263,'',422,47),(264,'Дякуємо за перегляд і до зустрічі в наступному відео!',433,47),(265,'',0,48),(266,'',10,48),(267,'今日のビデオはここまでです。ご視聴ありがとうございました。次の動画でお会いしましょう！',10,48),(268,'',20,48),(269,'',30,48),(270,'수책 잘한 척, 바베큐 좋은 하루 되세요.',30,48),(271,'',40,48),(272,'',50,48),(273,'',60,48),(274,'',70,48),(275,'',80,48),(276,'',90,48),(277,'',100,48),(278,'ཅས་ཅ་ནས་ཆ་འཔཨེ ྤ་་པ཰་ས་ད་འ་ཛ་ས་ན་མ་མ། མ་ནནྷ་པ་ནན་ན་བྷ་ཐ་ན་ན་མ་ན་མ་ད་ད་ཚ་མ་ན་',110,48),(279,'',120,48),(280,'',130,48),(281,'',140,48),(282,'우리 자꾸 말할 수 없어. 그래도 나랑 비슷하지?',150,48),(283,'',160,48),(284,'ಸಾನಾಂಡಿಲಗಝನಲತಾಂ ಉದಲಂ ದಾರವಾಲನಿರಕಾರಲನಿಸರಿಯನ ತದದದದದದರನಿರಕರಲನಾನಿರಕರಲನಿಸರನಿರಕರಲನಿ',170,48),(285,'',180,48),(286,'',190,48),(287,'',200,48),(288,'If you enjoyed the video, please subscribe and like it!',0,51),(289,'',10,51),(290,'',20,51),(291,'',30,51),(292,'',40,51),(293,'',50,51),(294,'',60,51),(295,'',70,51),(296,'',80,51),(297,'',90,51),(298,'最後まで視聴してくださり、本当にありがとうございます。',100,51),(299,'',110,51),(300,'',120,51),(301,'',130,51),(302,'',140,51),(303,'로고 이어주기, 이거 카운트 여기 써있어요. 이거 봐, 이거 봐. 잠시만요, 제가 테스트 해볼게요.',0,53),(304,'',10,53),(305,'그 자기 화면 한 번만 가려보시겠어요? 아! 캠, 캠, 캠! 10초 와줘야 되는데.',20,53),(306,'에프 스타넷 두 대 중 10초가량, 어 됐다, 됐다. 김 학생 바뀌었고, 31도.',30,53),(307,'그만 싸우겠어요. 어, 바뀌었다, 바뀌었다.',40,53),(308,'2시간이 지나고 퀵 퀴즈를 한 번만 보내볼게요.',50,53),(309,'채팅이 되고 질문하기에 답이 안 맞을 수도 있어요. 그냥 제가 아무거나 누른 거예요.',60,53),(310,'',70,53),(311,'답변해야지, 디메글을 하면 됩니다. 답변한 거예요? 아, 됐구나. 됐구나. 된다, 된다.',80,53),(312,'',0,55),(313,'',10,55),(314,'',20,55),(315,'',0,57),(316,'',10,57),(317,'I\'m going to go and start my server. Thank you. You\'re welcome. Thank you. Thank you. Thank you. Thank you.',20,57),(318,'If you enjoyed this video, be sure to check out my other videos.',30,57),(319,'1.5kg 치킨, 2.5kg 치킨, 3.5kg 치킨, 4.5kg 치킨, 5.5kg 치킨',40,57),(320,'',50,57),(321,'',60,57),(322,'',70,57),(323,'',80,57),(324,'',90,57),(325,'',100,57),(326,'이거 혹시... 사장님 피부타임에 들어가는 조건이 뭔지 아세요?',110,57),(327,'',120,57),(328,'스토어에서 피부타임이 있나요? 피부타임이 정말 절대 안 되어서 그거 빼고는 다 확인했는데.',130,57),(329,'',140,57),(330,'아, 일단 모니터가 너무 부담스러워서 이걸 10초 동안 유지해야 해. 모니터를 못 보고 몸이 안 움직이고 10초 이상.',150,57),(331,'죄송하지만, 제공된 텍스트는 수정할 내용이 없습니다. 다른 텍스트를 제공해 주시면 도와드리겠습니다.',160,57),(332,'이것들이 어떻게 된 소리야? 이거 복숭아를 진짜 조금씩 찍어도 돼.',170,57),(333,'Якщо вам подобається їжа, не забудьте поставити мені лайк і підписатися на канал!',0,58),(334,'',10,58),(335,'오후에 수고하세요. 다음 영상에서 만나요.',20,58),(336,'',30,58),(337,'오늘 영상은 여기까지입니다. 감사합니다.',40,58),(338,'Якщо вам подобається їжа, не забудьте поставити мені лайк і підписатися на канал!',50,58),(339,'한 번 더 줘봐. 치즈가 없어졌네. 치즈가 없어졌네. 치즈가 없어졌네. 치즈가 없어졌네.',60,58),(340,'그러면 내가 바로... 단순히 친구입니다. 남자 플레이, 남자 플레이, 남자 정오, 남자 정오, 남자 정오, 남자 닷, 남자 정말, 남자 정말, 남자 정오, 남자 정오.',70,58),(341,'',80,58),(342,'이 영상은 유료 광고를 포함하고 있습니다.',90,58),(343,'',100,58),(344,'',110,58),(345,'그 자막은 이번에 써본 편입니다. BYE',120,58),(346,'',130,58),(347,'지금 2번 한 표, 3번 한 표예요. 아, 이거 눈이 깜빡거리면서 나가네요. 미쳐버리겠네.',140,58),(348,'러시아어를 노리며 캐릭터들을 완성하는 건가요? 그게 영어밖에 안 되나요?',150,58),(349,'',160,58),(350,'',170,58),(351,'',180,58),(352,'',190,58),(353,'',200,58),(354,'여러분, 내 인내심이 너무 부족했던 거였구나.',210,58),(355,'그럼 프론트에서 처리를 잘못한 것 같아.',220,58),(356,'혹시 그 몇 초쯤인지 잘 읽으실 수 있으세요? 트루 반응한 거? 그거 타임라인 169부터 180까지, 169인가요?',230,58),(357,'감사합니다. 시청해 주셔서 감사합니다.',241,58),(358,'오늘 영상은 여기까지입니다! 다음 영상에서 만나요!',252,58),(359,'173부터 붙으시는 건가요? 173부터요? 네, 아쉬워요.',263,58),(360,'',274,58),(361,'근데 이거 거의 10초를 채우려면 고생 수준인데, 진짜 힘들어. 카메라 안 뜯고 계신가 봐. 아니, 아니, 그냥 진짜로 몸 조금만 움직여줘.',285,58),(362,'몸이 문제예요. 눈은 할 수 있는데, 몸이 진짜 조금만 움직여도 틀리거든요?',296,58),(363,'',306,58),(364,'',316,58),(365,'아, 이거 피버타임으로 바뀌는데 강사한테 피버타임이 떠야 하잖아요. 강사한테 넘어오는 게 없는데 173번부터 쭉 해서...',326,58),(366,'',336,58),(367,'네 스피버 스틱은 내일 들어와요.',346,58),(368,'',357,58),(369,'',368,58),(370,'',379,58),(371,'',390,58),(372,'',401,58),(373,'',412,58),(374,'',423,58),(375,'그냥 고통만 표시해도 되지 않을까요? 다시 해야 하나요? 필요한 거죠? 아니, 뭐.. 그것도 딱 하면 되는 거고, 아 그래요? 어려운 것들 많이 알려주시면 좋을 것 같아요.',434,58),(376,'',445,58),(377,'거치대 위치링을 이용해 계속 엔지니어 재료를 캄보디아에서 한글 인서트에 가져올 수 없습니다.',456,58),(378,'',466,58),(379,'',476,58),(380,'',486,58),(381,'',496,58),(382,'',506,58),(383,'',516,58),(384,'',526,58),(385,'',536,58),(386,'솔직히 배운 점은 아까 전에 했던 \'make a wish\'에 들어가면 되게 이런 것 같던데, 그때 그렇게 많이 간다는 건가? 혼자서 productive한 렌다 형... 생각만 해도 뱀뱀 아티보 참나, 진짜 몸무게 빼고 간 점이 돌아 1kg 그냥 오... 당연한 거로, vamos you 바이브 정답. 이제 진짜 오른손.',546,58),(387,'네? 우리 새벽에 5시까지 가면 그 매작집에 올게요. 도망가지 말고, 예. 랜턴, 랜턴 녀석들, 네 번째, 몇 번째 이제.',556,58),(388,'괜찮아? 아니, 지옥을 생각했었는데 그래서 이거 검토해.',566,58),(389,'무슨 도시락을 이렇게 시켰어? 아니, 뭐.',576,58),(390,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요. 안녕!',586,58),(391,'',596,58),(392,'Miniature Hip, Miniature Dance, Miniature Coach Melee, Miniature Hip Guys, Thank you for watching! Okay, ladies, make sure to watch, and don\'t forget to subscribe to my channel.',606,58),(393,'',616,58),(394,'',626,58),(395,'호시와 날씨가 어떻게 되었는지 궁금합니다. 감사합니다.',636,58),(396,'',646,58),(397,'',656,58),(398,'',666,58),(399,'',676,58),(400,'',686,58),(401,'테이블에 플레이하자. 대체 누가 맞는 거야? 잘 샀지? 먹어봐.',696,58),(402,'',706,58),(403,'',716,58),(404,'그냥 우는 거야? 어, 밀밀밥에 grosung이 넘기게 해서 호랑이 모래를 그려줬더니.',726,58),(405,'다음은 조금 더 심심한 것들이 있습니다.',0,59),(406,'뷔 형이 반박도 못했고 조금만 못했거든요. 근데 그거 원래 그런 거예요? 테스트 찍고 있는 거예요? 네, 지금.',10,59),(407,'',20,59),(408,'네, 돼요. 자리 비용이잖아요. 스피버 타이머입니다. 자리 비용 5초.',30,59),(409,'페부타인 삼촌이라고 하셨군요. 잠깐만요, 이거 풀어볼게요. 일단.',40,59),(410,'이제 10번이 됐다. 값은 한 번 바뀌어야 해.',50,59),(411,'그리고 그리고 그리고 그리고',60,59),(412,'',70,59),(413,'',80,59),(414,'',90,59),(415,'아, 엠티투, 엠티에 들어왔어요. 이걸 왜 못 가져오지?',100,59),(416,'아, 음식이 들어왔는데 피버요? 피버는 3초짜리니까.',110,59),(417,'',120,59),(418,'',130,59),(419,'이번 영상은 여기까지입니다. 다음 영상에서 만나요. 안녕.',140,59),(420,'请不吝点赞、订阅、转发和打赏支持《明镜》与《点点》栏目。',150,59),(421,'',160,59),(422,'',170,59),(423,'아, 두상 찍어버렸어요. [(두상 찍는 중)] [(두상 찍는 중)] [(두상 찍는 중)] [(두상 찍는 중)] 음, 좋아요.',180,59),(424,'',190,59),(425,'',200,59),(426,'아... 아니에요. 제가 알려드릴게요. 보아비아르.',210,59),(427,'天でフのズレしています。ああああああ、さあ.',220,59),(428,'네. 신물도 HugoJ의 작전이 하나가 되기 위해서는 처음부터 찍어야 해, 지금. 가면 안 돼, 있어. 미 searching.',230,59),(429,'',240,59),(430,'민준이 엉덩이 쓸데없는 거 다 한 거지? 우리 말해, 아 우리 말해요? 응, 민준이 엉덩이 쓸데없는 거 다 한 거지?',250,59),(431,'스크린샷.. 스크린샷 어떻게 읽지? 프린트 스크린, 컨트롤 시프트 아니에요? 그거 말고..',260,59),(432,'',270,59),(433,'숟가락으로 드립에 대한 미련 같은 리액션',0,61),(434,'아, 그냥 똑같이 하면 어떡해. 예의만 하면 꽂기.',10,61),(435,'',20,61),(436,'뷔는 아까 먹고 싶게 만들었지. 괜찮아, 너 위기해진 것 같다. 넌 뺏긴 것 같은데. 어, 넌, 너는, 너는, 너는, 너는, 넌, 너는, 너는, 너는, 너는, 너는, 너는, 너는, 너는, 너는, 너는, 너는, 너는, 너는.',30,61),(437,'근데 생각보다 많은데... 응, 근데 생각보다 많은데... 안 먹어? 왜? ㅋㅋ 죄송해요, 죄송해요.',40,61),(438,'아니야, 논이까지만 들어오지 마 [(책, 삼겹살, 사과, 볶음, 찬물 많이 먹여보기!] [(민지, 다 한 입 먹었어요!)',50,61),(439,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요. 안녕!',60,61),(440,'아까부터 죽은 거지? 아, 깜짝 놀랐어. 진짜 깜짝 놀랐어. 근데 이제는 깜짝 놀랐어.',70,61),(441,'애교 없이 노는 형, 오늘도 시청해 주셔서 감사합니다.',80,61),(442,'네, 여러분, 저는 이제 마치겠습니다. 이 영상에는 제 채널의 유료 광고가 포함되어 있습니다. 감사합니다.',0,60),(443,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요. 안녕!',10,60),(444,'That is meant to be time to get ready.',20,60),(445,'',31,60),(446,'아, 맛있다! 이게 맛있다! 이게 맛있다! 노랭이 맛인데, 노랭이 맛이지. 노랭이 맛!',41,60),(447,'화이팅 하셨어? 응, 숨이 과죽같이 꾸러기네. 이런 줄 아는구나. 눈치 잘 챘어.',52,60),(448,'내 이름은 뷔입니다. 나는 뷔입니다. 나는 뷔입니다. 나는 뷔입니다. 나는 뷔입니다.',62,60),(449,'',73,60),(450,'2부에 계속될 예정입니다. 4시까지 4시 정각에 시작합니다.',84,60),(451,'민규가 좋다고? 아니, 어? 나를 힘들게 만들었냐?',95,60),(452,'',106,60),(453,'',117,60),(454,'네, 이렇게 해서 이거는 맞아요. 손님이 손잡이를 찍어요. 지금이요? 지금이요. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네. 네.',127,60),(455,'제가 지금 열심히 Q&A를 하고 있는데, 이상한 댓글이 남아있어서요.',138,60),(456,'예, 정말 너무 좋겠죠. 성수님께서도 잘 부탁해 주신다니 정말 기대됩니다. 저도 아쉽지만 녹음하는 데에 죄송합니다.',148,60),(457,'제가 여러분이 만든 팁잇, 땀, 눈물을 알기 때문에 모든 페이지를 찍어서 정리했습니다. 팟땀, 맛땀 만드셨는데요.',158,60),(458,'본인이 해야죠. 반드시 기억해야 합니다. 솔직히 말하면 따라한 거 아시죠? 왠지 패스해봐야겠어요. 제가 좀...',168,60),(459,'',178,60),(460,'멈추지 않아도 되지만, 뒤에서 발음이 이상하다.',188,60),(461,'끝내보자. 싸울 것 같아. 멋지게 하는데?',198,60),(462,'아마 마스크와 모자를 뺐을 텐데, 뭐, 해발만 올려줘. 뭐에서 찍었죠? 아, 예. P, PMI.',208,60),(463,'',218,60),(464,'다음 영상에서 뵙겠습니다. 감사합니다.',228,60),(465,'',238,60),(466,'',248,60),(467,'',258,60),(468,'우리 멤버들, 오늘 Tisch 일자는 나지 않았어. 거짓말은 하지 않아. 틴탑과 멤버들 형식대로 멤버들을 보내내내.',268,60),(469,'일단 B에서 마스터 매직 게스트만 올리면 되죠.',279,60),(470,'이거 뭐지.. 짜라라라라라 탭 슈우우우우 원래 이렇게...',289,60),(471,'벌써 오랜 시간이 지난 hijos야. 치료가 끝났으니 리듬을 타고 더 운동을 시작합니다.',0,62),(472,'그런 사람을 너무 싫어해요. 제발 그냥 잘 되길 바래요.',10,62),(473,'',20,62),(474,'',30,62),(475,'편안하게 딱 이런 일입니다.',0,63),(476,'Якщо вам подобається їжа, не забудьте поставити мені лайк і підписатися на канал!',0,64),(477,'안녕하세요, 저는 유리입니다. 여러분, 제가 말씀드리고 싶은 것은...',10,64),(478,'',20,64),(479,'네, 그래요? 그런데 ABG 농도가 5천인데, 남성이 좀 많다고 하긴 하네요.',30,64),(480,'통계를 보고 있습니다. 제가 만들었죠. 아, 왜요? 강사인가요? 아니요, 아니요. 데이터베이스입니다. 아, 작곡가군요.',40,64),(481,'너무 좋다. 계속 불러봐야겠다. 진짜로. 그게 있어야 해석할 수 있다.',50,64),(482,'',60,64),(483,'',70,64),(484,'2. 1. 2. 3. 4. 5. 6. 7. 8. 9. 10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21.',80,64),(485,'',90,64),(486,'1.1.1.1.1 2.1.1.1.1 3.1.1.1.1 4.1.1.1.1 5.1.1.1.1 1.1.1.1.1',100,64),(487,'맡겨주고 싶어요. 켰거든요? 지금은 살아있어요.',0,65),(488,'오늘은 여기까지입니다. 다음 영상에서 또 만나요. 안녕!',10,65),(489,'화면에 제 얼굴이 진짜로 씌워 보여요. 진짜로 씌워 보여요. 진짜로 씌워 보여요.',20,65),(490,'',30,65),(491,'',40,65),(492,'',50,65),(493,'',60,65),(494,'네가 가장 잘 설명하는 자신. 자, 마무리.',0,67),(495,'오늘 영상은 여기까지입니다. 다음 영상에서 만나요. 안녕!',10,67),(10000,'그럼 이제 본격적으로 써피 소개부터 시작해보겠습니다. 네, 그러면 시스코치 분들의 경험담을 듣기에 앞서서 제가 먼저 사피 소개와 모집 정보에 대해서 안내를 좀 드려보겠습니다.',0,68),(10001,'네, 여러분들 사피에 대해서 이미 잘 알고 계시겠지만, 사피는 함께 가요 미래로 이네블링 피플이라고 하는 삼성의 사회공헌사업 모토가 있어요.',10,68),(10002,'그 비전과 함께 삼성의 소프트웨어 AI 교육 노하우가 잘 접목이 돼서 만들어진 프로그램입니다. 우수한 소프트웨어나 AI 전문가들을 양성하고 있고요.',20,68),(10003,'기업에는 저희가 양성한 인재들을 잘 공급하고 있고, 여러분들한테는 바로 청년 취업 기회를 만들어 드리도록 하고 있습니다.',30,68),(10004,'사피는 삼성뿐만 아니라 고용노동부도 함께 하고 있습니다. 그리고 최근에는 아이들과 미래재단, JA코리아 같은 비영리법인도 함께 여러분들의 취업서비스를 돕고 있습니다.',40,68),(10005,'그런 성공적인 프로그램입니다. 조금 더 자세히 한번 들어가 보죠. 우선 사피는 전국에 5개의 캠퍼스가 있어요. 전국 어디서나 가까이 있는 캠퍼스로 지원해서 들어갈 수 있는데요.',50,68),(10006,'서울에도 하나 있고, 대전, 광주, 구미 그리고 부산에 있는 부울경 캠퍼스까지 총 5개의 캠퍼스가 있습니다.',60,68),(10007,'모든 캠퍼스는 동일한 교육 인프라 환경을 갖추고 있고요. 취업지원센터도 모든 캠퍼스에 다 있습니다.',70,68),(10008,'뿐 아니라 강사님들도 각 지역 캠퍼스를 순회하면서 강의를 하고 있기 때문에, 어느 캠퍼스에서나 똑같은 실력을 갖출 수 있도록 지원해 드리고 있습니다.',80,68),(10009,'일단 사피에 교육생이 되면 다양한 혜택들이 있는데요. 실전형 소프트웨어 교육과 AI 교육이 전부 무료로 1년 동안 제공이 되고 있습니다.',90,68),(10010,'그 다음에 여러분들이 공부하고 있는 동안에 아르바이트나 용돈 걱정하지 말라고 매월 교육지원금을 100만원씩 드리고 있습니다.',100,68),(10011,'뿐만 아니라 서울을 제외한 대전, 광주, 구미, 부울경 캠퍼스는 정부에서 주는 훈련장려금 30만원도 추가로 받으실 수 있습니다.',110,68),(10012,'만약에 사피와서 열심히 공부해서 성적 우수자가 된다면 사피가 준비한 수많은 상품들이 여러분들한테 제공이 되고 있습니다.',120,68),(10013,'마지막으로 취업 성공까지 맞춤형 컨설팅이 제공되며, 1년의 교육과정은 전혀 길지 않고 아주 알찬 과정으로 구성되어 있습니다.',130,68),(10014,'준비가 되어 있습니다. 우선 처음 1학기 때 기본 과정을 수업 듣는데요. 이때는 알고리즘 기반의 기초 코딩 역량을 향상시켜 드리고,',140,68),(10015,'AI와 관련된 기초 지식이나 활용 역량 교육을 여러분들한테 실전형으로 실습을 하면서 수업을 들을 수 있도록 제공해 드리고 있습니다.',150,68),(10016,'두 번째 심화 과정은 2학기가 진행이 되는데요. 이때는 여러분들 팀을 잘 짜고 다양한 실전 소프트웨어 프로젝트들을 실습해 볼 수 있습니다.',160,68),(10017,'AI 기반의 자기주도형 프로젝트로 구성이 되어 있으며, 실제로 현업 개발자분들이 멘토로 참여하는 기업 연계 프로젝트들도 경험해 보실 수 있습니다.',170,68),(10018,'중간중간에는 \'잡페어\'라는 이름으로 여러분들이 취업에 필요한 다양한 역량들을 키워줄 수 있는 그런 교육 프로그램이 제공됩니다.',180,68),(10019,'자기소개서, IT 포트폴리오 작성, 면접 대비 등 취업에 필요한 실전 연습을 충분히 하실 수 있도록 저희가 도와드리고 있습니다.',190,68),(10020,'또한 사피 전용 채용박람회를 통해 수많은 기업의 인사채용 담당자들과 직접 면접이나 상담을 할 수 있는 기회도 얻어 가실 수 있습니다.',200,68),(10021,'앞에서 말씀드렸던 취업지원 서비스에 대해서 조금 더 설명드리겠습니다. 사피에 들어가면 취업이 잘 된다는 얘기는 숫자가 증명을 해주고 있습니다.',210,68),(10022,'지금까지 누적 취업률이 85%나 되고 있습니다. 일단 사피에 들어오기만 하면 85%는 다 취업에 성공하고 있다는 얘기고, 이 수치는 계속 올라가고 있습니다.',220,68),(10023,'이렇게 취업이 잘 되는 이유가 있습니다. 사피 교육생들을 채용한 많은 기업들이 사피 출신들은 정말 일도 잘하고 회사 생활도 잘한다고 정평이 나 있습니다.',230,68),(10024,'그래서 많은 기업들이 사피 출신들을 더 많이 채용하기 위해 서류 심사 면제나 코딩 테스트 면제 같은 수많은 우대 조건들을 제시하고 있습니다.',240,68),(10025,'혹은 사피에서의 1년을 경력 1년으로 인정해 주어 연봉을 올려주는 기업들도 있으며, 기업들의 신뢰가 매우 두터운 편입니다.',250,68),(10026,'심지어 신한은행 같은 경우는 아예 사피생들만 지원할 수 있는 별도의 특별전형을 열어주시기도 합니다.',260,68),(10027,'금융권 취업을 희망하시는 교육생분들이 많으실 텐데, 이런 특별전형은 정말 놓치기 아까운 기회가 될 것입니다.',270,68),(10028,'이런 우대 기업들도 지금 현재 한 170여 개 있는데, 계속해서 증가하고 있는 추세에 있습니다. 또한 5대 은행과 업무 협약을 체결했습니다.',280,68),(10029,'신한, KB국민, 하나, 우리, NH농협은행 등 연봉이 높은 은행들에 사피 출신들이 더 많이 취업할 수 있는 기회들을 제공해주고 있습니다.',290,68),(10030,'취업지원 서비스 중 유명한 3가지를 간추려보자면, 우선 모든 캠퍼스에 취업지원센터가 있고 전문 컨설턴트가 상주하고 있다는 점입니다.',300,68),(10031,'입학부터 취업 성공 시점까지 모의 면접이나 자기소개서 컨설팅 같은 개인별 맞춤형 취업 컨설팅을 지속적으로 제공해 드리고 있습니다.',310,68),(10032,'뿐만 아니라 많은 기업들이 사피 캠퍼스에 직접 찾아와 채용 설명회를 열어드리고 있으며, 채용 TO가 생길 때마다 정보를 공유해 드립니다.',320,68),(10033,'이를 통해 교육생들은 더 많은 취업 기회와 양질의 정보들을 얻어갈 수 있으며, 기업 인사담당자들과 직접 소통할 기회도 얻게 됩니다.',330,68),(10034,'마지막으로는 사피 커뮤니티를 통해 수료생들과의 네트워킹 기회도 제공하여 실질적인 현업의 조언을 들을 수 있도록 돕고 있습니다.',340,68),(10035,'매년 두 번 진행하는 전용 채용박람회를 통해 인사채용 담당자들과 상담이나 면접을 해볼 수 있으며, 실제 입사로 연결되는 사례도 많습니다.',350,68),(10036,'그럼 이렇게 좋은 사피에 어떻게 들어갈 수 있는지 간단하게 프로세스를 설명드릴게요. 이미 10월 27일부터 15기 지원서 접수가 시작되었죠.',360,68),(10037,'사피닷컴에 들어가면 바로 지원할 수 있는데, 절차가 매우 간단해서 빨리 하시는 분들은 1분 이내에도 접수를 마칠 수 있다고 합니다.',370,68),(10038,'PC와 모바일 모두 가능하며, 유튜브 설명을 듣는 동안 간단하게 접수해주시면 이벤트 추첨을 통해 햄버거 세트도 받으실 수 있으니 잊지 마세요.',380,68),(10039,'지원 접수가 마감된 후인 11월 11일부터는 에세이를 작성해서 제출하는 기간입니다. 이 기간을 놓치지 않도록 주의하셔야 합니다.',390,68),(10040,'합격을 위해서 본인의 경험을 미리미리 정리해 두시는 것이 큰 도움이 될 것입니다. 에세이 제출은 합격의 지름길이 될 수 있습니다.',400,68),(10041,'에세이 제출 이후에는 본격적인 선발 프로세스가 진행될 예정이니, 끝까지 집중해서 준비해 주시기 바랍니다.',410,68),(10042,'500자 내외로 본인의 경험과 생각을 적어 제출해 주세요. 이때 작성한 에세이가 인터뷰 질문으로 나올 수 있으니 답변 가능한 내용으로 작성하는 것이 좋습니다.',420,68),(10043,'11월 중순에는 적성 진단이 예정되어 있습니다. 소프트웨어 전공자와 비전공자는 서로 다른 시험을 보게 되며, 전공자는 코딩 테스트를 치르게 됩니다.',430,68),(10044,'비전공자분들은 기초 논리 테스트를 보게 됩니다. 이 절차가 모두 끝나고 나면 12월 중에 최종적으로 대면 또는 비대면 인터뷰가 진행될 예정입니다.',440,68),(10045,'인터뷰에서는 사회 동향에 대한 관심도와 소프트웨어 기반의 문제 해결 능력, 사고력 등을 확인하며 지원자의 열정과 협력 가능성을 중점적으로 봅니다.',450,68),(10046,'또한 사피에서 동료들과 함께 성실히 공부할 준비가 되었는지 확인하는 과정이 포함됩니다. 지원을 위해서는 다음 세 가지 조건을 충족해야 합니다.',460,68),(10047,'사피는 청년들을 위한 사업이므로 만 29세 이하를 대상으로 모집합니다. 졸업 예정자나 이미 졸업하신 분들 모두 지원 자격에 해당합니다.',470,68),(10048,'이러한 지원 자격과 모집 요건을 꼼꼼히 유의하셔서 남은 지원 프로세스를 차질 없이 진행해 주시기 바랍니다.',480,68),(10049,'이번 15기 기준으로 96년 1월 1일 이후 출생하신 29세 이하이면서, 4년제 대학 졸업자나 졸업 예정자라면 누구나 지원이 가능합니다.',490,68),(10050,'전공이나 학과는 전혀 상관없습니다. 현재 사피에는 경영, 음악, 미술 등 다양한 분야의 비전공 학생들이 들어와서 열심히 공부하고 있습니다.',500,68),(10051,'누구라도 지원할 수 있지만 현재 미취업자여야 한다는 조건은 중요합니다. 사피는 청년 취업 문제를 해결하기 위해 만들어진 사업이기 때문입니다.',510,68),(10052,'구체적으로는 12월 인터뷰 전까지 재직 중이 아님을 확인할 수 있는 서류를 제출해 주시면 되며, 졸업 예정자는 취업 여부와 관계없이 지원 가능합니다.',520,68),(10053,'이러한 자격 요건을 꼼꼼히 확인하셔서 지원 시 불이익이 없도록 유의하시기 바랍니다. 사피는 여러분의 열정과 도전을 언제나 응원하고 있습니다.',530,68),(10054,'모집 요강을 다시 한번 숙지하신 뒤 지원해 주시기 바라며, 궁금한 점은 언제든 사피 홈페이지를 통해 확인하실 수 있습니다.',540,68),(10055,'여러분의 새로운 시작을 사피가 함께하겠습니다. 망설이지 말고 지금 바로 도전해 보세요. 감사합니다.',550,68),(10056,'마지막으로 교육 시작일에 입과가 가능해야 하며, 1년 동안 사피 교육에 온전히 몰입할 수 있는 준비가 되신 분들이 지원하실 수 있습니다.',560,68),(10057,'본인 스스로 통학이 가능해야 하고, 해외여행 결격 사유가 없어야 합니다. 또한 졸업을 위한 계절 학기 수업 등의 일정이 겹치면 안 됩니다.',570,68),(10058,'이런 부분들이 모두 준비된 분들의 지원을 부탁드립니다. 많은 분이 궁금해하시는 적성 진단에 대해 간단히 설명을 드리겠습니다.',580,68),(10059,'소프트웨어 전공자분들은 지원서 작성 시 선택한 전공에 따라 온라인 CBT 기반의 코딩 테스트를 보게 된다고 생각하시면 됩니다.',590,68),(10060,'코딩 테스트에 관한 자세한 팁은 뒤에서 다시 한번 설명해 드릴 예정이며, 전공자 역량을 충분히 발휘할 수 있는 문제들로 구성됩니다.',600,68),(10061,'비전공자분들은 코딩 테스트 대신 향후 개발자나 AI 전문가가 될 수 있는 사고 능력을 갖췄는지 확인하는 기초 논리력 테스트를 보게 됩니다.',610,68),(10062,'마찬가지로 온라인 CBT 기반으로 진행되며, 기초 논리력 테스트와 주관식 문항 등으로 구성되어 논리적 사고 과정을 평가하게 됩니다.',620,68),(10063,'전공 여부와 상관없이 누구나 풀 수 있는 문제들이지만, 오늘 라이브 끝까지 함께하시면 뒤에서 직접 문제풀이를 통해 출제 유형을 경험하실 수 있습니다.',630,68),(10064,'마지막 인터뷰는 사피의 인재상을 확인하는 자리입니다. 현재 실력도 중요하지만 논리적 사고력과 학습에 대한 강한 의지, 취업에 대한 열정을 중점적으로 봅니다.',640,68),(10065,'동료들과 협업하며 즐겁게 공부할 수 있는 태도를 갖춘 분이라면, 현재 소프트웨어 역량이 부족하더라도 누구든 사피의 교육생이 될 수 있습니다.',650,68),(10066,'인터뷰에서는 평소 IT에 대한 관심도와 개발자로서 필요한 사고력 및 추론 능력을 확인하며, 1년 과정을 완수할 열정이 있는지를 질문하게 될 것입니다.',660,68),(10067,'지금까지 사피 소개와 15기 합격 전략을 간략히 설명드렸습니다. 이어서 변화된 사피의 AI 중심 교육 프로그램에 대해서도 추가로 안내해 드리겠습니다.',670,68),(10068,'끝까지 집중해 주시면 합격 확률을 높일 꿀팁을 얻으실 수 있습니다. 특히 새롭게 도입된 실습 중심 프로젝트는 현업 실전 역량을 키우는 데 최적화되어 있습니다.',680,68),(10069,'기대하셔도 좋습니다. 여러분의 도전을 진심으로 응원하며, 함께 성장해 나갈 수 있기를 바랍니다. 그럼 본격적인 세부 내용을 확인해 보시죠.',690,68),(9999999,'그럼 이제 본격적으로 써피 소개부터 시작해보겠습니다. 네, 그러면 시스코치 분들의 경험담을 듣기에 앞서서 제가 먼저 사피 소개와 모집 정보에 대해서 안내를 좀 드려보겠습니다.',0,68),(10000000,'그래서 이렇게 하고 일단,',0,108),(10000001,'시험 때는 소리를 틀 거예요.',3,108),(10000002,'이렇게 소리도 좀 틀어봅시다.',6,108),(10000003,'감사합니다.',9,108),(10000004,'보여줄게.',12,108),(10000005,'인터넷에서 이미 잘 알고 계시겠지만,',15,108),(10000006,'세훈이 얼굴을 한 번 보여주면 더 좋을 것 같아요.',18,108),(10000007,'그래서 희한하게 학생의 얼굴을',21,108),(10000008,'시청해 주셔서 감사합니다.',24,108),(10000009,'팀이 목소리를 내서 목숨을 건너오는 것도 테스트해줍니다.',27,108),(10000010,'될 것 같습니다.',30,108),(10000011,'다들 잘 부탁드립니다.',33,108),(10000012,'왜요?',36,108),(10000013,'Silence.',39,108),(10000014,'감사합니다.',42,108),(10000015,'어? 그러네요. 왜요?',45,108),(10000016,'왜 안 되지?',48,108),(10000017,'어쨌든 보입니다.',51,108),(10000018,'지금 되나요?',54,108),(10000019,'',57,108),(10000020,'감사합니다.',0,111),(10000021,'안녕!',3,111),(10000022,'좋습니다.',6,111),(10000023,'네, 그럼 이제 본격적으로 시작하겠습니다.',0,113),(10000024,'서피 소개부터 하겠습니다.',3,113),(10000025,'시스코치분들의 경험담을 듣기 전에',6,113),(10000026,'먼저 사피 소개와 모집 정보에 대해 안내하겠습니다.',9,113),(10000027,'드려보겠습니다. 네, 여러분.',12,113),(10000028,'사피에 대해 이미 잘 알고 계시겠지만',15,113),(10000029,'함께 가요, 미래로! 이네이빌링 피플이라고 하는 삼성의 사회공헌 사업.',18,113),(10000030,'비전과 함께 삼성의 소프트웨어가 있습니다.',21,113),(10000031,'AI 교육 노하우가 잘 접목되어 만들어진 프로그램입니다.',24,113),(10000032,'우수한 소프트웨어 및 AI 전문가들을 양성하고 있습니다.',28,113),(10000033,'기업에는 저희가 양성한 인재들을 잘 공급하고 있습니다.',32,113),(10000034,'청년들에게는 바로 청년 취업 기회를 제공하도록 하고 있습니다.',35,113),(10000035,'사피는 삼성뿐만 아니라 고용노동부도',39,113),(10000036,'',43,113),(10000037,'국회재단, JA코리아 같은 비영리 단체인데도',46,113),(10000038,'여러분의 취업 서비스를 돕는 성공적인 프로그램이 되길 바랍니다.',49,113),(10000039,'이 프로그램입니다.',53,113),(10000040,'자, 자세히 한번 들어보죠. 우선 사피는 전구에 다섯 개.',56,113),(10000041,'캠퍼스가 있습니다. 전국 어디서나 가까이에 있습니다.',59,113),(10000042,'있는 캠퍼스에 지원해서 들어갈 수 있는데요. 서울에도 하나 있습니다.',62,113),(10000043,'대전, 광주, 구미, 그리고 부산에 있는 부울경 캠퍼스까지',66,113),(10000044,'총 다섯 개의 캠퍼스가 있습니다.',70,113),(10000045,'이 캠퍼스는 동일한 교육 인프라와 환경을 갖추고 있습니다.',73,113),(10000046,'취업지원센터는 모든 캠퍼스에 있습니다.',76,113),(10000047,'강사님들도 각 지역 캠퍼스를 순회하면서',80,113),(10000048,'하고 있기 때문에 여러분이 어느 캠퍼스에서',83,113),(10000049,'누구나 똑같은 실력을 갖출 수 있도록 지원해 주시기 바랍니다.',86,113),(10000050,'해드리고 있습니다.',89,113),(10000051,'사피의 교육생이 되면 다양한 혜택이 있습니다.',93,113),(10000052,'수단형 소프트웨어 교육과 AI 교육이 전부 무료로 1년 동안 제공됩니다.',96,113),(10000053,'제공되고 있고요, 그 다음에 여러분들이 공부하고 있는...',99,113),(10000054,'자, 서울을 제외한',0,114),(10000055,'네, 그럼 이제 본격적으로 서피 소개를 시작하겠습니다.',3,114),(10000056,'시작해 보겠습니다.',6,114),(10000057,'제가 먼저 사피의 소개와 모집 정보에 대해 알려드리겠습니다.',9,114),(10000058,'좀 드려보겠습니다. 네, 여러분들 사피에 대한 이미지를',13,114),(10000059,'잘 알고 계시겠지만, 사피는 함께 갑니다.',17,114),(10000060,'이래로 \'이네이빌링 피플\'이라고 하는 삼성의 사회공헌 사업 모토가 있어요.',20,114),(10000061,'비전과 함께하는 삼성의 소프트웨어 AI 교육',24,114),(10000062,'노하우가 잘 접목되어 만들어진 프로그램입니다.',27,114),(10000063,'우수한 소프트웨어 및 AI 전문가들을 양성하고 있습니다.',30,114),(10000064,'기업에는 저희가 양성한 인재들을 잘 공급하고 있습니다.',33,114),(10000065,'여러분에게는 바로 청년 취업 기회로 돌아갈 수 있는 기회가 됩니다.',36,114),(10000066,'만들어 드리겠습니다.',39,114),(10000067,'삼성뿐만 아니라 고용노동부도 함께하고 있습니다. 그리고',43,114),(10000068,'최근에는 아이들과 미래재단, 재이앤에이 코리아 같은',47,114),(10000069,'',0,117),(10000070,'15기 입시설명 온라인 모집 설명에',0,117);
/*!40000 ALTER TABLE `script` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session` (
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `date` date NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `end_at` time NOT NULL,
  `janus_room_id` bigint DEFAULT NULL,
  `pin` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quiz_count` int NOT NULL,
  `recording_id` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_at` time NOT NULL,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `trainer_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `use_emotion` bit(1) NOT NULL,
  `use_focus` bit(1) NOT NULL,
  `use_quiz` bit(1) NOT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`session_id`),
  KEY `FK5ibwg70x5r9hls6pqe5yg2vvb` (`course_id`),
  CONSTRAINT `FK5ibwg70x5r9hls6pqe5yg2vvb` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session`
--

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` VALUES (1,'2026-02-04 14:34:57.609417','2026-02-04','세션 생성 테스트 중입니다.2','17:00:00',NULL,NULL,1,'UoQRuyoYVpVHJFx6rpUxHu95Hd4KSbjRv2snQmxA','10:11:00','테스트 세션 6',4,'2026-02-04 17:34:26.891827',_binary '',_binary '',_binary '',1),(2,'2026-02-04 14:45:32.039762','2026-02-04','세션 생성 테스트 중입니다.2','18:00:00',NULL,NULL,1,'cNIaSQSxPdM4tGzOIaJmbruG0RTFihwjZbSLpHz5','14:45:00','테스트 세션 6-t',6,'2026-02-04 17:09:49.421515',_binary '',_binary '',_binary '',2),(3,'2026-02-04 14:53:17.871689','2026-02-15','OAuth2와 JWT를 활용한 인증/인가 구현 실습 세션입니다.','16:00:00',NULL,NULL,1,'lbNb5Xlmku8ozSh0UMMogkTXTJahiK6RvdttjKCm','14:00:00','스프링 시큐리티 심화 학습',5,'2026-02-04 14:53:17.871706',_binary '',_binary '',_binary '',3),(4,'2026-02-04 15:51:09.337862','2026-02-05','추가추가루굼ㄴㅇㄹ','23:02:00',NULL,NULL,1,'BtwdBZ4nF6XL7LxnNeEpC2jRuiIJStNUsXGJYVNF','01:00:00','새로운 세션 추가',6,'2026-02-04 15:51:09.337882',_binary '',_binary '',_binary '',2),(5,'2026-02-04 15:57:49.411661','2026-02-06','rgrgrg','15:00:00',NULL,NULL,3,'dMQq80TTZehKG5kGNN00TSQItVOsnGZQTIlPwUS5','11:57:00','rrtg',6,'2026-02-04 15:57:49.411683',_binary '',_binary '',_binary '',2),(6,'2026-02-04 16:50:12.541095','2026-02-12','ㅁㄴㅇㄻㄹㄴㅇㅋㄴㅇㄹ','16:54:00',NULL,NULL,1,'iuqcHbA7UtQ4pIMVeoqNHzLititdAnqgPnu6da9c','18:50:00','ㅁㄴㅇㅁㄴㅇㄻㄴㅇㄹ',4,'2026-02-04 16:50:12.541116',_binary '',_binary '',_binary '',4),(7,'2026-02-04 16:50:38.318773','2026-02-04','ㄴㅇㄹㄴㅇㅎㄴㄴㄹㅇ','16:56:00',26020438453,'166882',1,'8ugBZGgIxHK38eI28LZERC9glmkBiqollaBoeAmm','16:50:00','ㅀㅎㄴㅇㄹㄴㄹ',4,'2026-02-04 16:50:44.304599',_binary '',_binary '',_binary '',4),(8,'2026-02-04 17:10:07.355955','2026-02-04','ㅎㄴㅇㄴㅇㅎ','17:11:00',NULL,NULL,1,'y6PAWFubRPipIiyXGBYjtjTjlYcgEbsS7T8CF7Tp','17:10:00','ㅗㅀㅀ',6,'2026-02-04 17:14:25.494156',_binary '',_binary '',_binary '',2),(9,'2026-02-04 17:38:13.549346','2026-02-05','ㄴㄹㄴㅇㄹ','17:42:00',NULL,NULL,1,'bKV9ZZ7wUoaDBLLbxECpfX62siwyXsUPtpzmtCT2','09:45:00','ㅇㄹㄴㅀ',4,'2026-02-05 09:49:04.384964',_binary '',_binary '',_binary '',1),(10,'2026-02-05 09:49:32.334067','2026-02-05','dfsdfsg','19:53:00',26020529519,'879600',1,'tpE4oD0a60HnoiKfzUwMPymHQZdwMVOeEDdMmwlK','09:49:00','sddfdf',4,'2026-02-05 10:44:22.707944',_binary '',_binary '',_binary '',1),(11,'2026-02-05 10:46:06.882506','2026-02-05','787878','12:47:00',NULL,NULL,1,'gbR3LZiAm0R6nCzfuwVoLg0vbyXqZCmUFfBauLhe','10:46:00','85788',6,'2026-02-05 13:08:50.747532',_binary '',_binary '',_binary '',2),(12,'2026-02-05 13:12:20.868803','2026-02-05','afsdadfsfasd','23:16:00',NULL,NULL,1,'rHtIWWCLMQVlYnj50jRL92C05foNZdsbQB56XxwX','13:12:00','asdffdsa',6,'2026-02-05 13:23:18.585144',_binary '',_binary '',_binary '',2),(13,'2026-02-05 13:15:44.093882','2026-02-05','dasfadfadfs','21:15:00',NULL,NULL,1,'34A2s6Z936eQksuxI1FmMVLleQAA5xnlXkVDVaEm','13:15:00','dafdasfadsf',6,'2026-02-05 14:52:11.138756',_binary '',_binary '',_binary '',2),(14,'2026-02-05 13:24:24.940358','2026-02-05','adfasdfasdf','17:24:00',NULL,NULL,1,'sj6IgNoyxTNl59Hbg1xg21ymWJSVA4sgsSWeoOmN','13:24:00','adsfadfsadsfafdsadfsafds',6,'2026-02-05 13:27:51.313235',_binary '',_binary '',_binary '',2),(15,'2026-02-05 13:28:16.595407','2026-02-05','ㅁㄹㅇㄴㅁㅇㄴㄻ','18:28:00',NULL,NULL,1,'79yM5dxjDM8VRYtBgHE0VDeTAMKyBhozyNsEzCm8','13:28:00','세션세션세션1123',6,'2026-02-05 13:31:59.941936',_binary '\0',_binary '',_binary '',2),(16,'2026-02-05 13:32:15.600073','2026-02-05','123341234','13:37:00',NULL,NULL,1,'sRyWFfEKojsVbUqcBUNMDbtjxNtb6Cy3OTZhhChQ','13:32:00','세선셔성라ㅓㅁ인러밍나러ㅣㅏㅁㄴ어',6,'2026-02-05 13:40:36.664890',_binary '',_binary '',_binary '',2),(17,'2026-02-05 13:41:02.755032','2026-02-05','ㅇㄻㄴㅇㄻㅇㄹㅈㅇㄻㄷㄱㅋㅌㅊㅁㄷㅈㄱㅁㄴㄻㄴㅇㄻㄴ','20:40:00',NULL,NULL,1,'SnBSBMISnEj9zxhQXHDJZSWITJuvlTBCHUcZRgu7','13:40:00','ㅇㅁㄻㅇㄴㄻㅇㄴㄻㅇㄻㅇㄻㅇㄻㅇㄻㄴㅇㄻㅇㄹ',6,'2026-02-05 13:42:06.167306',_binary '',_binary '',_binary '',2),(18,'2026-02-05 13:42:51.808165','2026-02-05','ㄷㅈㅂㄱㅂㄷㄱㅈㅂㅈㄱㄷㅂㄷㅈㄱ','19:43:00',NULL,NULL,1,'j6FV70E5pC8EqpWnZ8n9PMkO2dr7nePs7iEW04te','13:42:00','ㅈㄷㅁㄷㅈㄷㄱㅈㅂㅈㄷㄱㅂ',6,'2026-02-05 14:05:50.197391',_binary '',_binary '',_binary '',2),(19,'2026-02-05 14:06:00.383586','2026-02-05','sdffgssg','19:05:00',NULL,NULL,1,'9X3kGVhiWhbehJr6p947dJ1HnvZJ7Z8XONOWXHq3','14:05:00','sddgfdfs',6,'2026-02-05 14:14:49.178830',_binary '',_binary '',_binary '',2),(20,'2026-02-05 14:15:01.035450','2026-02-05','afsdasfsfadafdasdf','21:14:00',NULL,NULL,1,'GZTaYIWhfIWFvJctSp72IoyDIDm1FrwTDtcrXFZD','14:14:00','sddafsadsadf',6,'2026-02-05 14:27:03.051032',_binary '',_binary '',_binary '',2),(21,'2026-02-05 14:27:23.486604','2026-02-05','dhfdfdghgh','19:27:00',NULL,NULL,1,'98vSDa2pzHdA89kc2D7zQZbP9pWx3N2lJ70ZiqnJ','14:27:00','gbvghfgdf',6,'2026-02-05 14:51:49.304065',_binary '',_binary '',_binary '',2),(22,'2026-02-05 14:41:49.930954','2026-02-05','adfsfasdfsd','18:44:00',NULL,NULL,1,'IrxCsp4ZBbNB9Cnsd5fKlW1CUjpDMjgN3KVPs3ji','14:41:00','sdafdfadfsafsd',6,'2026-02-05 14:51:34.687433',_binary '',_binary '',_binary '',2),(23,'2026-02-05 14:52:28.407417','2026-02-05','ㅇㄹㄴㅀㄶ','20:57:00',NULL,NULL,1,'99bOquxyayoaBwvoL4UT6f7o31UuWwzsSW5ecFde','14:52:00','ㄹㅇㄴㅁㅀ',6,'2026-02-05 14:58:13.312744',_binary '',_binary '',_binary '',2),(24,'2026-02-05 14:58:23.719524','2026-02-05','ㅀㄴㅇㅀㄴ','20:58:00',NULL,NULL,1,'O7Tb4MWZUUdAEvEjkspzn66yaDP8lChOQ7oZKqkD','14:58:00','ㄴㅇㅇㅀ',6,'2026-02-05 15:06:10.272204',_binary '',_binary '',_binary '',2),(25,'2026-02-05 15:06:20.651581','2026-02-05','ㄴㅇㅀㄴㅇㅀㄴㅇㅀ','22:12:00',26020555297,'200638',1,'wsgzoEuAnBx58jaUIoJB3KQ0kruMrBPEsAIul5zw','15:06:00','ㄴㄹㅇㅎㄴㅀㄴㅇㅀ',6,'2026-02-05 15:06:36.568157',_binary '',_binary '',_binary '',2),(26,'2026-02-05 15:14:41.569886','2026-02-05','ㅁㅇㄴㄻㅇㄹㄴ','20:14:00',NULL,NULL,1,'vp48fDLyEXm8pbvZmQvpKdzRYIpHQVtOLNa6h8PF','15:14:00','ㅁㄴㅇㄹㄻㅇㄴ',6,'2026-02-05 15:17:51.938677',_binary '',_binary '',_binary '',2),(27,'2026-02-05 15:18:03.794453','2026-02-05','ㅈㄷㅅㄷㄱㅈㅈ','20:23:00',26020537477,'987513',1,'D3rzOyXKq3hrHzc1qzYz6NQhbOVmpGFEho2LjT4j','15:18:00','ㅈㄷㄱㄷㄷㄱ',6,'2026-02-05 15:18:09.707302',_binary '',_binary '',_binary '',2),(28,'2026-02-05 15:20:19.422360','2026-02-05','로ㅓㄹ허로','19:24:00',26020566744,'819482',1,'oJdtuZjo9nlNeWHjDSn3J9vQxgD2ootiNLFB3DJu','15:20:00','ㅕㅛㅓ러ㅏㄹ',6,'2026-02-05 15:20:44.977561',_binary '',_binary '',_binary '',2),(29,'2026-02-05 15:29:24.126596','2026-02-05','ㅇㄴㄻㄴㅇㄻㄴㅇㄻㅇㄴㄹ','20:29:00',26020582591,'413806',1,'EjYhDvtjdLBZuD98HM2cq4iWFVYsLrcakQHEumkh','15:29:00','ㄹㄷㅁㄴㅇ',6,'2026-02-05 15:31:02.523181',_binary '',_binary '',_binary '',2),(30,'2026-02-05 15:33:09.866226','2026-02-05','1234','22:33:00',NULL,NULL,1,'1oId2088qeXUM0sYad0yp2ust3RkRUodiIxPNDQU','15:33:00','프로님 안녕하세요',6,'2026-02-05 15:49:25.651566',_binary '',_binary '',_binary '',2),(31,'2026-02-05 15:49:44.125776','2026-02-05','ㅁㅇㄴㄻㄴㅇㄻㅇㄹㄴ','21:49:00',NULL,NULL,1,'FE4OHYXoLh29LBU4EmWJxlzt8WT6ybmAe3mwePiN','15:49:00','ㄹㅇㅁㄴㅇㄴㄻㄴㅇㄹ',6,'2026-02-05 16:16:06.700237',_binary '',_binary '',_binary '',2),(32,'2026-02-05 16:16:21.792344','2026-02-05','adsfadsfas','16:21:00',NULL,NULL,1,'ud7w3lcwvCI1WKvjeBAG84bOx9I2IJgPdvf2jmEw','16:16:00','dfafdsf',6,'2026-02-05 16:19:51.477180',_binary '',_binary '',_binary '',2),(33,'2026-02-05 16:42:33.298423','2026-02-05','dasfdsfadsfdfs','22:42:00',NULL,NULL,1,'xZzOYxvTj3ceq2CUIpWlsgnNf7pZAtZ1iYVNb6Dw','16:42:00','afsdasdfdssdf',6,'2026-02-05 16:46:21.124895',_binary '',_binary '',_binary '',2),(34,'2026-02-05 16:53:33.623841','2026-02-05','asfdafsd','21:58:00',NULL,NULL,1,'rANki4z1C6D0adCYrqRcF3NgjJLdtaLzOl786M4O','16:53:00','sdaffsad',6,'2026-02-05 17:14:43.059589',_binary '',_binary '',_binary '',2),(35,'2026-02-05 17:14:23.229488','2026-02-06','ASDFASDFASDFASD','17:14:00',NULL,NULL,1,'PHyawUq7s1xwZpXXA6QTYT4e8tk5uAtajQpW41SH','17:14:00','DFASFASDFASDF',6,'2026-02-05 17:14:23.229505',_binary '',_binary '',_binary '',2),(36,'2026-02-05 17:15:18.898744','2026-02-05','DASFASDFAFSD','23:15:00',NULL,NULL,1,'4uOyBbrAopJqcZHY1M975AbvnEZpJ1jv9iq3vc6O','17:15:00','SAAFASDFDASF',6,'2026-02-05 17:23:09.393594',_binary '',_binary '',_binary '',2),(37,'2026-02-05 17:24:32.296463','2026-02-05','ㄹㅀㄹㄹㅀ','23:30:00',NULL,NULL,1,'oq6U0w7xqQl3OCAvnBRd1FwUfjNK19LpJ88j0MeJ','17:24:00','ㅀㅀㄽㅀ',6,'2026-02-05 17:28:24.737092',_binary '',_binary '',_binary '',2),(38,'2026-02-06 08:50:40.990888','2026-02-06','werqerq','11:46:00',NULL,NULL,1,'HuoO1zPGVgmgrzxPFcUBeIrEcgZpr5GOLaJHvuKi','08:50:00','erqrwqe',6,'2026-02-06 08:54:33.400794',_binary '',_binary '',_binary '',2),(39,'2026-02-06 08:54:54.561251','2026-02-06','gdffsgd','11:54:00',NULL,NULL,1,'ETatbskrKReXbzOK8r3m8n6aFDmJSQk4FPDA0rFH','08:54:00','rthhgffg',6,'2026-02-06 08:58:42.345650',_binary '',_binary '',_binary '',2),(40,'2026-02-06 08:58:54.586663','2026-02-06','adfsasf','11:58:00',NULL,NULL,1,'oscp3tukjI52ZpC1LBzvHEIsjxjJ6faGTeXZ8asi','08:58:00','edfad',6,'2026-02-06 09:03:05.615620',_binary '',_binary '',_binary '',2),(41,'2026-02-06 09:10:44.157032','2026-02-06','dfssdfsfd','09:10:00',NULL,NULL,1,'KtJAvnX4PeietYnrT1Iq8UhGY5ebg4oRBsuWlef4','09:10:00','zxcxdfsdf',6,'2026-02-06 09:10:44.157046',_binary '',_binary '',_binary '',2),(42,'2026-02-06 09:11:41.796424','2026-02-06','fsdsfsfd','09:11:00',NULL,NULL,1,'dRYVcPzimDSX1XiFnX63RwPULCKbrZ9sktjADwIR','09:11:00','fggfssdfgs',6,'2026-02-06 09:11:41.796438',_binary '',_binary '',_binary '',2),(43,'2026-02-06 09:12:05.345607','2026-02-06','adfsasdsdfsd','11:13:00',NULL,NULL,1,'v8zo5dh54udKDZI1RO0gLMfGF0IUbu6xd53Da065','09:11:00','sdafdsasdv',6,'2026-02-06 09:31:06.843884',_binary '',_binary '',_binary '',2),(44,'2026-02-06 09:31:16.366290','2026-02-06','qerqwerqewrqe','10:32:00',NULL,NULL,1,'s2ULuu3q0rh8IH4fDSJfRnMLIEKMqkNeuXZbR9r4','09:31:00','aeewrqewrqeweqwrqewr',6,'2026-02-06 09:34:07.902016',_binary '',_binary '',_binary '',2),(45,'2026-02-06 09:34:25.682033','2026-02-06','231412344123','10:36:00',NULL,NULL,1,'iU91jMKPlZAWjrglqb52ZXYU6md3QHYx54ZMRgQT','09:34:00','2341412343241',6,'2026-02-06 09:39:24.270698',_binary '',_binary '',_binary '',2),(46,'2026-02-06 09:39:39.872793','2026-02-06','rqwerqwerqwer','10:40:00',NULL,NULL,1,'lsYOPsktNZRjr2rniqqQM4e4HlOQMcoYFNOm6nmf','09:39:00','werqwerqewrqe',6,'2026-02-06 09:43:07.593783',_binary '',_binary '',_binary '',2),(47,'2026-02-06 09:43:23.451907','2026-02-06','qewrqrwe','11:44:00',NULL,NULL,1,'BC4fIJc3us60xNk1bY6P8vcdA4kEyA6lYvZrP2uB','09:43:00','ewqrqwer',6,'2026-02-06 10:01:06.698493',_binary '',_binary '',_binary '',2),(48,'2026-02-06 10:01:24.252979','2026-02-06','ㅀㄴㅇㅀㄹㄴㅇㅎ','11:03:00',NULL,NULL,1,'kA3F1vTB7Ei2jiF0TN09MKM3NLgHjDUIzBKzAdOa','10:01:00','ㅇㅀㄴㅇㅀㄴㅇㅀ',6,'2026-02-06 10:35:42.725412',_binary '',_binary '',_binary '',2),(49,'2026-02-06 10:33:02.258917','2026-02-06','test1','16:00:00',NULL,NULL,3,'aiztHu0uSgRjabq51kHwtI68GYODfIFn4XSOJcY2','14:00:00','test',6,'2026-02-06 10:33:02.258928',_binary '',_binary '',_binary '',5),(50,'2026-02-06 10:35:59.404263','2026-02-06','ㅁㅇㄴㄻㄹㅇ','11:37:00',26020671322,'592991',1,'ydpz6arOgraP78W6ZJNYTvQ1PHFDDTy6xaut1avq','10:35:00','ㅇㄻㅇㄴㄻㅇㄹㄴ',6,'2026-02-06 10:37:36.623246',_binary '',_binary '',_binary '',2),(51,'2026-02-06 10:37:32.115417','2026-02-06','ㅁㅇㄴㄻㅇㄴㄹ','11:38:00',NULL,NULL,1,'rGkNWHXJ1SWPzQ9q5sNzFzHvv8TUobDcKQZbI6Lo','10:37:00','ㅇㅁㄴㄻㅇㄴㄹ',6,'2026-02-06 10:51:55.911323',_binary '',_binary '',_binary '',2),(52,'2026-02-06 10:52:14.429315','2026-02-06','123312123','11:53:00',NULL,NULL,1,'3LDJKLSko9IjG1NRUQy2JK9F0imt0xWHIWsqIyI3','10:52:00','테스트용 세션1',6,'2026-02-06 10:52:14.429325',_binary '',_binary '',_binary '',2),(53,'2026-02-06 11:00:00.693632','2026-02-06','테스트용 세션','11:38:00',NULL,NULL,1,'JWsYCnXwxyh5vTec7Qr02LtxcpKNm3Pmj23OsJWK','10:59:00','테스트용 세션',6,'2026-02-06 11:08:28.213620',_binary '',_binary '',_binary '',2),(54,'2026-02-06 11:53:35.909050','2026-02-06','feadadsfadsfadsf','14:55:00',26020699378,'746964',1,'6kqq0cHqMU01dkjL23FNyONDicx4PQqU4PdPkRV5','11:53:00','adsfasdf',6,'2026-02-06 11:53:46.017932',_binary '',_binary '',_binary '',2),(55,'2026-02-06 12:07:04.048054','2026-02-06','adsfafdsadsf','17:06:00',NULL,NULL,1,'oktXavZfl2ItZNihyTncPEfp8ffOudNsiYi2sUZo','12:06:00','adsfdadf',6,'2026-02-06 12:09:57.350088',_binary '',_binary '',_binary '',2),(56,'2026-02-06 12:10:17.974035','2026-02-06','qerqerqerq','15:13:00',NULL,NULL,1,'47p4LYx9gSrt7BLL3vmUyZ92ESzociZtTgGbY3LL','12:10:00','erqwqer',6,'2026-02-06 12:17:32.192075',_binary '',_binary '',_binary '',2),(57,'2026-02-06 12:17:44.398654','2026-02-06','adfsafdsafds','14:19:00',NULL,NULL,1,'jdxpPJFjbhUlXQma9SpQoh8BFE2QSCTvze0u78T1','12:17:00','asdfdasf',6,'2026-02-06 12:24:00.879698',_binary '',_binary '',_binary '',2),(58,'2026-02-06 12:24:26.218636','2026-02-06','sdsgds','15:27:00',NULL,NULL,1,'s6SfCC5FrBfJP1ATpyUvdEby1ZWVP8kTV3znVMBn','12:24:00','fsfgsgdsfg',6,'2026-02-06 12:48:04.485146',_binary '',_binary '',_binary '',2),(59,'2026-02-06 12:48:35.689713','2026-02-06','ㅁㄴㅇㄻㄹㄴㅇㅁㄹㄴㅇ','14:50:00',NULL,NULL,1,'rFyLJ6GzyUZZMUKBoHGMrwNQTJmFeqYcHo6t2uC8','12:48:00','ㅁㄴㅇㄹㄻㄴㅇㅁㄹㅇㄴ',6,'2026-02-06 12:57:18.892334',_binary '',_binary '',_binary '',2),(60,'2026-02-06 12:51:18.539186','2026-02-06','제로 트러스테에 대한 이해를 갖추겠습니다.','14:23:00',NULL,NULL,3,'o69XCB58J3a6fU9XFnIGMVBeVGgqzQ5bdGbjItRO','12:53:00','1주차 - 제로 트러스트에 대한 이해',6,'2026-02-06 13:05:56.453804',_binary '',_binary '',_binary '',6),(61,'2026-02-06 12:57:34.322670','2026-02-06','adsfasdafsd','16:00:00',NULL,NULL,1,'vYQuYVEda2U6cfiPi0FynAEMdE4dON8uaqWzzKBm','12:57:00','sdfaasfadsf',6,'2026-02-06 13:01:57.821819',_binary '',_binary '',_binary '',2),(62,'2026-02-06 13:02:28.782562','2026-02-06','dsfadsfafdsafds','16:02:00',NULL,NULL,1,'T9f9Z6D3KNVrtEYeP03i3z2HJOUORRghScZobPYZ','13:02:00','sdfasdf',6,'2026-02-06 13:05:57.836378',_binary '',_binary '',_binary '',2),(63,'2026-02-06 13:06:31.354329','2026-02-06','asdfadfs','15:07:00',NULL,NULL,1,'V8znybaHUT23gM5nUsLYPgwnHdRPKRrWoOEesWwy','13:06:00','asdfadsf',6,'2026-02-06 13:08:11.427036',_binary '',_binary '',_binary '',2),(64,'2026-02-06 13:08:25.477338','2026-02-06','fadsafds','15:10:00',26020620933,'161231',1,'zsAdxXzyDnUbG31HPtA6AjQQpGHHtWl1CdI8g8mK','13:08:00','sdaffad',6,'2026-02-06 13:12:04.856089',_binary '',_binary '',_binary '',2),(65,'2026-02-06 13:12:42.048383','2026-02-06','sadfsdafafsd','13:13:00',NULL,NULL,1,'UOtPCJpVAAGkvWrV8zjGjZF1lsjCESTAuuC7Na9Z','13:12:00','asdfdaf',6,'2026-02-06 13:15:49.754656',_binary '',_binary '',_binary '',2),(66,'2026-02-06 13:16:02.786907','2026-02-06','weqreqwrqrwe','15:18:00',NULL,NULL,1,'c4vGbK5uPj05KG5JVFYRdtH9DCJytfL2mCteEjil','13:15:00','ereqwrweqr',6,'2026-02-06 13:21:25.991325',_binary '',_binary '',_binary '',2),(67,'2026-02-06 13:21:44.799431','2026-02-06','afsdasdf','14:22:00',NULL,NULL,1,'1iqOPZP5ogy5UAGflzL9vFjnG7P2uwpESoBABoVH','13:21:00','fadsasdf',6,'2026-02-06 13:24:11.877953',_binary '',_binary '',_binary '',2),(68,'2026-02-06 13:49:27.290089','2026-02-06','SSAFY 15기 온라인 모집 설명회 세션','14:45:00',NULL,NULL,2,'YdBlvc8IwT9Dw0XpPNJOu9bktVaBb4FYjW8sUvgo','14:30:00','SSAFY 15기 온라인 모집 설명회',15,'2026-02-06 14:43:00.698799',_binary '',_binary '',_binary '',7),(70,'2026-02-06 14:17:37.575918','2026-02-06','ssgsg','16:19:00',26020621305,'247362',1,'p64VFZ5F8s8r1xQTd8fID1TJLJ6t1ZhPcUqY2ow4','14:17:00','gffsdgf',6,'2026-02-06 15:42:04.661500',_binary '',_binary '',_binary '',2),(75,'2026-02-06 14:53:18.999725','2026-02-06','SSAFY 15기 온라인 모집 설명회1','15:07:00',NULL,NULL,2,'CPntUcq6FJdyuu86IfWF9Bc7Fj6aF6fabRQ0fSh1','14:53:00','SSAFY 15기 온라인 모집 설명회1',15,'2026-02-06 14:55:29.043292',_binary '',_binary '',_binary '',7),(76,'2026-02-06 15:57:18.303864','2026-02-06','ㅇㄴㅁㄹㅇㄴ','16:58:00',NULL,NULL,1,'ePlXmr0dPbYDWdGT4S9UQDLFVcTjMLQsg87XUwN6','15:57:00','ㅇㄴㅇㄴㅇㄹ',6,'2026-02-06 16:00:05.007098',_binary '\0',_binary '',_binary '',2),(77,'2026-02-06 16:03:10.556114','2026-02-06','ㅁㅇㄹㄴㅁㄴㅇㄻㄹㅇㄴ','17:04:00',NULL,NULL,1,'XnSs475iMSikJ4i30p5n1CrSHkZEN91bit9nroJC','16:03:00','ㅇㄴㅁㄹㄻㅁㄴㄹㅇ',6,'2026-02-06 16:04:32.119604',_binary '',_binary '',_binary '',2),(78,'2026-02-06 16:06:23.992235','2026-02-06','ㄴㄹㄶㄶ','17:07:00',NULL,NULL,1,'jZ7OTDtac10TZWlmfSY0sCl3e0GfkKYE21GarOwq','16:06:00','ㄴㅇㅀㄴㅀㄴㅀ',6,'2026-02-06 16:08:08.674428',_binary '',_binary '',_binary '',2),(79,'2026-02-06 16:09:18.377014','2026-02-06','fasddfasdfasdfasdf','20:13:00',NULL,NULL,1,'4daL0ivMCiavlbAv6MsGP7HO4o3eXb9liABYWggE','16:09:00','asdadsffasd',6,'2026-02-06 16:10:40.409552',_binary '',_binary '',_binary '',2),(80,'2026-02-06 16:36:37.819578','2026-02-06','ㅁㄴㅇㄹㄻㄴㅇ','17:37:00',NULL,NULL,1,'HMlbUvlRrzWgBYi7mNMOXPmJwAqDBC5lgZ4u3pBH','16:36:00','ㄴㅇㄻㄴㄹㅇ',6,'2026-02-06 16:45:44.668372',_binary '',_binary '',_binary '',2),(82,'2026-02-06 19:20:11.721583','2026-02-06','시연 연습1','23:25:00',NULL,NULL,1,'QTgSTSbcMDYlQJV49oxZaQ8uCSFPx7gytj5s47dB','19:20:00','시연 연습1',15,'2026-02-06 19:36:42.700852',_binary '',_binary '',_binary '',7),(83,'2026-02-07 14:49:18.728496','2026-02-07','','16:50:00',NULL,NULL,1,'YaeRD731RpRl2R2WGNizg60z9vDaiMeR2kwqrmgq','14:50:00','SSAFY 15기 온라인 모집 설명회',15,'2026-02-07 14:58:35.781724',_binary '',_binary '',_binary '',7),(84,'2026-02-07 16:05:44.488916','2026-02-07','test2222','17:05:00',NULL,NULL,1,'I04Ucdxzj4kqJL9XUzKaTqvNkA0qtVDoEcKguxKC','16:07:00','test2',6,'2026-02-07 16:14:38.759694',_binary '',_binary '',_binary '',5),(85,'2026-02-08 10:30:03.353475','2026-02-08','SSAFY 14기와의 만남','00:33:00',NULL,NULL,1,'UEx6lNbESMXobnRKu2sjclXRWdlJTZ0XeooTAlxy','10:30:00','SSAFY 14기와의 만남',15,'2026-02-08 10:30:03.353510',_binary '',_binary '',_binary '',7),(86,'2026-02-08 12:08:44.759883','2026-02-08','adsfasfd','15:12:00',26020865278,'220104',1,'QfUyA63egs4PCjr4YupOFUGMeAoetBk3OuzBn6lU','12:08:00','sxafsasdf',6,'2026-02-08 12:08:53.889283',_binary '',_binary '',_binary '',2),(87,'2026-02-08 12:48:59.324175','2026-02-08','ㅁㄹㅇㄴㅁㄹㅇㄴ','15:51:00',NULL,NULL,1,'vQ6d2MT7Jfg0Qsky6ArjRMcXADQr7d9xK5FoVK9E','12:48:00','ㅇㅁㄴㄻㄹㅇㄴ',6,'2026-02-08 12:56:42.219824',_binary '',_binary '',_binary '',2),(88,'2026-02-08 12:57:01.416272','2026-02-08','987864849','14:58:00',NULL,NULL,1,'fZixpoqXExNgOJjXjxBIyw6AIgraT1YzMcvfEapX','12:56:00','6556465',6,'2026-02-08 13:17:15.537855',_binary '',_binary '',_binary '',2),(89,'2026-02-08 13:17:25.332422','2026-02-08','sadfsafd','14:18:00',NULL,NULL,1,'dMTFreF4T2BAJdE6PWx9c9fU2XUtwGM7NO3NG769','13:17:00','safdasfd',6,'2026-02-08 13:22:56.030045',_binary '',_binary '',_binary '',2),(90,'2026-02-08 13:25:25.122830','2026-02-08','ㅁㄴㅇㄻㄴㅇㄹ','15:27:00',26020838940,'374422',1,'suyb5h8v0Ye8rkTstTz9FCunVesJp5icRkL8xTi6','13:25:00','ㄴㅁㅇㄹㅇㄴㄹ',6,'2026-02-08 13:25:29.305643',_binary '',_binary '',_binary '',2),(91,'2026-02-08 16:03:39.756601','2026-02-08','asfdadsf','18:06:00',26020889318,'093848',1,'bDPA4PrHcUvBZKWIIqugjBaXcMUsbWyTvfAtp6gg','16:03:00','afdsasdf',6,'2026-02-08 16:03:44.467241',_binary '',_binary '',_binary '',2),(92,'2026-02-08 16:07:17.025828','2026-02-08','asdfasdfasdf','17:08:00',26020827840,'912561',1,'x0n9mHGArWDmyYKVbqHLTL3V5uWdTsdZIdLa4tTA','16:07:00','asdfasdf',6,'2026-02-08 16:07:24.598134',_binary '',_binary '',_binary '',2),(93,'2026-02-08 16:17:55.502043','2026-02-08','ewqreqwr','17:18:00',NULL,NULL,1,'lciuWnZiL27mOTOAcRyJUKD90My59clnUEAViY1X','16:17:00','qwerqwer',6,'2026-02-08 16:18:23.960823',_binary '',_binary '',_binary '',2),(94,'2026-02-08 16:18:48.962200','2026-02-08','qewrqewr','17:19:00',NULL,NULL,1,'62jRDPtfZJg1FGctZ9Qj2QBjqv1yVuN1eiQtOURc','16:18:00','erqwqewr',6,'2026-02-08 16:19:37.687026',_binary '',_binary '',_binary '',2),(95,'2026-02-08 16:58:17.943891','2026-02-08','ㄴㅇㄻㄴㅇㄹ','18:58:00',NULL,NULL,1,'yNBuzDqvnkikA7XXJg2Cl9BOe985ZixU4DEBlMR5','16:58:00','ㅇㄴㅁㄻ',6,'2026-02-08 16:59:18.134287',_binary '',_binary '',_binary '',2),(96,'2026-02-08 17:25:24.461448','2026-02-09','Kafka란 무엇인가? ','22:26:00',NULL,NULL,1,'rPbJ9dXKDyzK9vKVeS6ySIL121O85p7qJ5OhWRE0','09:40:00','Kafka란 무엇인가? ',15,'2026-02-08 17:25:24.461464',_binary '',_binary '',_binary '',10),(98,'2026-02-08 17:58:18.087411','2026-02-08','set','18:10:00',NULL,NULL,1,'keZ4dkYy4j3k360vtONAu9IUH7lEZYy2vXWWTrGt','17:56:00','test',15,'2026-02-08 18:06:06.514392',_binary '',_binary '',_binary '',10),(104,'2026-02-08 19:00:36.114115','2026-02-07','어떻게 제로 트러스트를 구현할 것인가','16:55:00',NULL,NULL,1,'QF4Lfx681Af8CszvBLZfSJitLQTTMZjW1lzcfGfb','15:33:00','2주차 - 어떻게 제로 트러스트를 구현할 것인가',6,'2026-02-08 19:00:36.114163',_binary '',_binary '',_binary '',6),(107,'2026-02-08 19:28:38.767126','2026-02-08','카프카란?','19:31:00',NULL,NULL,1,'iGtqS5rTLJ3UAAd77dK7JBQGSMZ5XbrQlpkJFMld','19:28:00','시연2',15,'2026-02-08 19:30:43.593326',_binary '',_binary '',_binary '',10),(108,'2026-02-08 19:40:35.903481','2026-02-08','카프카 아키텍쳐','19:50:00',NULL,NULL,1,'siI5e0jiSoL0JQvnlVX9iaplGXKguCpluuNA4rAU','19:40:00','카프카 아키텍쳐',15,'2026-02-08 19:51:41.047770',_binary '',_binary '',_binary '',10),(111,'2026-02-08 20:30:38.922343','2026-02-08','카푸카','20:37:00',NULL,NULL,1,'vm1z1d9elBvjkWmz7ZJlbQdL3wV2rOfq63kG0iwy','20:30:00','카프카구조서영',15,'2026-02-08 20:34:01.120467',_binary '',_binary '',_binary '',10),(113,'2026-02-08 20:34:40.080857','2026-02-08','녹화테스트 들어오지마세요','20:39:00',NULL,NULL,1,'7qiDXczZAtCdSHHjEaaZu7MNuyyqrZ98MIeQgYcG','20:34:00','녹화테스트 들어오지마세요',15,'2026-02-08 20:37:56.178276',_binary '',_binary '',_binary '',10),(114,'2026-02-08 20:38:25.216841','2026-02-08','노컴','20:42:00',NULL,NULL,1,'XxJ5O4lvVHarPnAw0MGwirA2WqtZcrEkhau7HSmK','20:38:00','노컴',15,'2026-02-08 20:39:47.113459',_binary '',_binary '',_binary '',10),(116,'2026-02-08 21:03:31.843626','2026-02-08','kafka 란 무엇인가','21:30:00',26020830829,'885384',1,'lmnoKWFfFTgOMelpxF9jH36XcKRaWYuWhoyYK6gb','21:03:00','kafka 란 무엇인가',15,'2026-02-08 21:03:34.780397',_binary '',_binary '',_binary '',10),(117,'2026-02-08 21:13:55.128268','2026-02-08','실시간 강의 ','21:20:00',NULL,NULL,1,'nz005WFTgcsi7Clnewk1IaSvZ04uC2BaNCqIziya','21:13:00','실시간 강의 ',15,'2026-02-08 21:18:50.702180',_binary '',_binary '',_binary '',10);
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session_attachment`
--

DROP TABLE IF EXISTS `session_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_attachment` (
  `attachment_id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `FK5wfwlv1aofkjv3tuqwj9h7wff` (`session_id`),
  CONSTRAINT `FK5wfwlv1aofkjv3tuqwj9h7wff` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`) ON DELETE CASCADE,
  CONSTRAINT `FKoqyec1u6lb9iilf9fagh8tadf` FOREIGN KEY (`attachment_id`) REFERENCES `attachment` (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session_attachment`
--

LOCK TABLES `session_attachment` WRITE;
/*!40000 ALTER TABLE `session_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `session_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session_chatting_message`
--

DROP TABLE IF EXISTS `session_chatting_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_chatting_message` (
  `session_chatting_message_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `session_chatting_room_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`session_chatting_message_id`),
  KEY `FK5oguo9a7qko3kq7amag5fqy3u` (`session_chatting_room_id`),
  KEY `FK5xlkbwyfrtdo14s11x5ueyfjd` (`workspace_member_id`),
  CONSTRAINT `FK5oguo9a7qko3kq7amag5fqy3u` FOREIGN KEY (`session_chatting_room_id`) REFERENCES `session_chatting_room` (`session_chatting_room_id`),
  CONSTRAINT `FK5xlkbwyfrtdo14s11x5ueyfjd` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session_chatting_message`
--

LOCK TABLES `session_chatting_message` WRITE;
/*!40000 ALTER TABLE `session_chatting_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `session_chatting_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session_chatting_room`
--

DROP TABLE IF EXISTS `session_chatting_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_chatting_room` (
  `session_chatting_room_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`session_chatting_room_id`),
  UNIQUE KEY `UKrq3oe6672qeu61xhqekwdppnk` (`session_id`),
  CONSTRAINT `FKgldov375oysd4mvkapj1ogt6h` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session_chatting_room`
--

LOCK TABLES `session_chatting_room` WRITE;
/*!40000 ALTER TABLE `session_chatting_room` DISABLE KEYS */;
/*!40000 ALTER TABLE `session_chatting_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workspace`
--

DROP TABLE IF EXISTS `workspace`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspace` (
  `workspace_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `description` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_searchable` bit(1) NOT NULL,
  `logo_url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`workspace_id`),
  KEY `FKs1pc16it05s2x892g8m8q2h9t` (`member_id`),
  CONSTRAINT `FKs1pc16it05s2x892g8m8q2h9t` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workspace`
--

LOCK TABLES `workspace` WRITE;
/*!40000 ALTER TABLE `workspace` DISABLE KEYS */;
INSERT INTO `workspace` VALUES (1,'2026-02-04 14:12:28.324767',NULL,'새로 만든 설명tttt',_binary '','더미 - S3 연동 이후 URL이 들어감','에듀맥스 워크스페이스','2026-02-05 12:59:35.336542',3),(2,'2026-02-04 17:40:13.755686',NULL,'와우와우',_binary '','더미 - S3 연동 이후 URL이 들어감','신규신규','2026-02-04 17:40:13.755701',3),(5,'2026-02-05 14:05:06.954777',NULL,'테스트중',_binary '','더미 - S3 연동 이후 URL이 들어감','새로만듦','2026-02-05 14:05:06.954870',3),(6,'2026-02-06 09:40:34.833154',NULL,'싸피 15기 모집을 위한 홍보공간',_binary '','https://d285ryfcbcw5yb.cloudfront.net/ssafy_profile.png','싸피 15기','2026-02-06 09:40:34.833180',13),(7,'2026-02-06 09:52:37.090493',NULL,'시연 데이터용',_binary '','https://d285ryfcbcw5yb.cloudfront.net/ssafy_profile.png','싸피 14기','2026-02-06 09:52:37.090507',13),(8,'2026-02-06 12:10:56.963034',NULL,'test',_binary '','더미 - S3 연동 이후 URL이 들어감','test','2026-02-06 12:10:56.963046',3),(9,'2026-02-06 14:02:48.737527',NULL,'121234',_binary '','더미 - S3 연동 이후 URL이 들어감','으악','2026-02-06 14:02:48.737544',3),(10,'2026-02-07 23:01:20.396061',NULL,'워크스페이스 로고 테스트ddd',_binary '',NULL,'워크스페이스 로고','2026-02-08 01:40:04.107593',3),(11,'2026-02-08 01:40:50.632013',NULL,'테스트\n',_binary '',NULL,'테스트합니다','2026-02-08 01:40:50.632034',3),(12,'2026-02-08 19:15:50.477759',NULL,'ㅎㅇ',_binary '',NULL,'ㅎㅇ','2026-02-08 19:15:50.477775',3),(13,'2026-02-08 21:12:06.270115',NULL,'ssafy',_binary '',NULL,'SSafy','2026-02-08 21:12:06.270129',13);
/*!40000 ALTER TABLE `workspace` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workspace_member`
--

DROP TABLE IF EXISTS `workspace_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspace_member` (
  `workspace_member_id` bigint NOT NULL AUTO_INCREMENT,
  `connected_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `role` enum('ADMIN','LEARNER','TRAINER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `workspace_id` bigint NOT NULL,
  PRIMARY KEY (`workspace_member_id`),
  KEY `FKhpy1tx8cq3yeopjowr477p8hu` (`member_id`),
  KEY `FKqt5hp8najyhtbees6p095nn16` (`workspace_id`),
  CONSTRAINT `FKhpy1tx8cq3yeopjowr477p8hu` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
  CONSTRAINT `FKqt5hp8najyhtbees6p095nn16` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`workspace_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workspace_member`
--

LOCK TABLES `workspace_member` WRITE;
/*!40000 ALTER TABLE `workspace_member` DISABLE KEYS */;
INSERT INTO `workspace_member` VALUES (1,NULL,'2026-02-04 14:33:59.977454','TRAINER','ACTIVE','2026-02-08 00:55:51.818840',4,1),(2,NULL,'2026-02-04 14:38:20.799516','TRAINER','ACTIVE','2026-02-04 14:38:20.799534',6,1),(3,NULL,'2026-02-04 14:40:01.368835','LEARNER','ACTIVE','2026-02-04 14:40:01.368861',7,1),(4,NULL,'2026-02-04 14:51:45.813171','TRAINER','ACTIVE','2026-02-04 14:51:45.813190',1,1),(5,NULL,'2026-02-04 14:53:03.247088','TRAINER','ACTIVE','2026-02-04 14:53:03.247106',5,1),(6,NULL,'2026-02-04 15:09:30.782834','LEARNER','ACTIVE','2026-02-05 15:55:49.816399',2,1),(9,NULL,'2026-02-05 14:15:52.333597','LEARNER','ACTIVE','2026-02-05 15:55:49.816399',9,1),(10,NULL,'2026-02-05 14:32:10.538238','LEARNER','ACTIVE','2026-02-08 00:53:21.199112',10,1),(11,NULL,'2026-02-05 16:45:58.384828','TRAINER','ACTIVE','2026-02-08 00:57:26.665397',11,1),(12,NULL,'2026-02-05 17:14:38.579740','TRAINER','ACTIVE','2026-02-05 17:32:57.104519',2,2),(13,NULL,'2026-02-06 13:19:10.900094','TRAINER','ACTIVE','2026-02-06 13:22:46.951298',15,6),(14,NULL,'2026-02-06 13:19:13.795758','LEARNER','ACTIVE','2026-02-06 13:19:13.795774',14,6),(15,NULL,'2026-02-06 13:19:14.920245','LEARNER','ACTIVE','2026-02-06 13:19:14.920265',17,6),(16,NULL,'2026-02-06 13:19:16.210166','LEARNER','ACTIVE','2026-02-06 13:19:16.210184',16,6),(17,NULL,'2026-02-06 13:19:17.625557','LEARNER','ACTIVE','2026-02-06 13:19:17.625575',18,6),(18,NULL,'2026-02-06 17:25:34.359266','TRAINER','ACTIVE','2026-02-08 00:56:00.283034',21,1),(19,NULL,'2026-02-07 15:41:10.875693','TRAINER','ACTIVE','2026-02-09 03:21:15.570933',22,1),(20,NULL,'2026-02-08 01:03:17.872313','LEARNER','ACTIVE','2026-02-08 01:39:28.531237',6,10),(21,NULL,'2026-02-08 01:05:10.793524','LEARNER','ACTIVE','2026-02-08 01:39:09.761466',10,10),(22,NULL,'2026-02-08 01:05:38.172380','LEARNER','ACTIVE','2026-02-08 01:05:38.172397',21,10),(23,NULL,'2026-02-08 01:28:15.713783','LEARNER','ACTIVE','2026-02-08 01:28:15.713798',9,10),(24,NULL,'2026-02-08 19:54:33.292867','TRAINER','INACTIVE','2026-02-08 19:54:53.776428',16,7),(25,NULL,'2026-02-09 08:41:45.238498','LEARNER','ACTIVE','2026-02-09 08:41:45.238530',14,13);
/*!40000 ALTER TABLE `workspace_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workspace_register`
--

DROP TABLE IF EXISTS `workspace_register`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspace_register` (
  `workspace_register_id` bigint NOT NULL AUTO_INCREMENT,
  `approved_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `reject_reason` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reject_type` enum('AFFLIATION_MISMATCH','ETC','INVITE_CODE_USE_MISS','OVER_CAPACITY') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('APPROVE','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `workspace_id` bigint NOT NULL,
  PRIMARY KEY (`workspace_register_id`),
  KEY `FKh8wkl5w535fe1kf3k4b5pk8b3` (`member_id`),
  KEY `FKdlgsucfcqnwiin2qc29tkl9pt` (`workspace_id`),
  CONSTRAINT `FKdlgsucfcqnwiin2qc29tkl9pt` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`workspace_id`) ON DELETE CASCADE,
  CONSTRAINT `FKh8wkl5w535fe1kf3k4b5pk8b3` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workspace_register`
--

LOCK TABLES `workspace_register` WRITE;
/*!40000 ALTER TABLE `workspace_register` DISABLE KEYS */;
INSERT INTO `workspace_register` VALUES (1,'2026-01-26 06:23:38.102000','2026-02-04 14:32:57.357296',NULL,NULL,'APPROVE','2026-02-04 14:33:59.982486',4,1),(2,'2026-01-26 06:23:38.102000','2026-02-04 14:38:04.012551',NULL,NULL,'APPROVE','2026-02-04 14:38:20.801874',6,1),(3,'2026-01-26 06:23:38.102000','2026-02-04 14:38:12.438453',NULL,NULL,'APPROVE','2026-02-04 14:40:01.371429',7,1),(4,'2026-02-02 21:00:00.000000','2026-02-04 14:44:23.806398',NULL,NULL,'APPROVE','2026-02-04 15:09:30.784813',2,1),(5,'2026-02-02 21:00:00.000000','2026-02-04 14:51:40.868613',NULL,NULL,'APPROVE','2026-02-04 14:51:45.815347',1,1),(6,'2026-02-02 21:00:00.000000','2026-02-04 14:52:59.131435',NULL,NULL,'APPROVE','2026-02-04 14:53:03.261646',5,1),(7,'2026-02-04 20:07:23.312235','2026-02-04 20:07:23.312235',NULL,NULL,'APPROVE','2026-02-05 17:14:38.581847',2,2),(10,'2026-02-05 14:15:10.585341','2026-02-05 14:15:10.585341',NULL,NULL,'APPROVE','2026-02-05 14:15:52.336489',9,1),(13,'2026-02-05 14:31:50.049450','2026-02-05 14:31:50.049450',NULL,NULL,'APPROVE','2026-02-05 14:32:10.540448',10,1),(14,'2026-02-05 14:39:57.018704','2026-02-05 14:39:57.018704',NULL,NULL,'APPROVE','2026-02-05 16:45:58.387852',11,1),(15,'2026-02-06 13:11:10.114105','2026-02-06 13:11:10.114105',NULL,NULL,'APPROVE','2026-02-06 13:19:10.902749',15,6),(16,'2026-02-06 13:13:07.102388','2026-02-06 13:13:07.102388',NULL,NULL,'APPROVE','2026-02-06 13:19:13.797719',14,6),(17,'2026-02-06 13:13:20.708322','2026-02-06 13:13:20.708322',NULL,NULL,'APPROVE','2026-02-06 13:19:14.922112',17,6),(18,'2026-02-06 13:13:39.975351','2026-02-06 13:13:39.975351',NULL,NULL,'APPROVE','2026-02-06 13:19:16.211831',16,6),(19,'2026-02-06 13:13:55.628800','2026-02-06 13:13:55.628800',NULL,NULL,'APPROVE','2026-02-06 13:19:17.627357',18,6),(20,'2026-02-06 17:25:28.406405','2026-02-06 17:25:28.406405',NULL,NULL,'APPROVE','2026-02-06 17:25:34.361064',21,1),(21,'2026-02-07 15:35:01.357164','2026-02-07 15:35:01.357164',NULL,NULL,'APPROVE','2026-02-07 15:41:10.877386',22,1),(22,'2026-02-08 01:02:36.355016','2026-02-08 01:02:36.355016',NULL,NULL,'APPROVE','2026-02-08 01:03:17.874211',6,10),(23,'2026-02-08 01:03:43.009167','2026-02-08 01:03:43.009167',NULL,NULL,'APPROVE','2026-02-08 01:05:38.174239',21,10),(24,'2026-02-08 01:04:25.506800','2026-02-08 01:04:25.506800',NULL,NULL,'APPROVE','2026-02-08 01:05:10.795306',10,10),(25,'2026-02-07 16:28:15.936000','2026-02-08 01:26:40.875131',NULL,NULL,'APPROVE','2026-02-08 01:28:15.715654',9,10),(26,'2026-02-08 03:08:24.461000','2026-02-08 03:04:46.409942',NULL,NULL,'REJECTED','2026-02-08 03:08:24.293771',11,10),(27,'2026-02-08 19:54:33.164000','2026-02-08 19:54:11.970469',NULL,NULL,'APPROVE','2026-02-08 19:54:33.294574',16,7),(28,NULL,'2026-02-08 21:02:33.605208',NULL,NULL,'PENDING','2026-02-08 21:02:33.605224',14,7),(29,'2026-02-09 08:41:44.898000','2026-02-08 21:12:58.176436',NULL,NULL,'APPROVE','2026-02-09 08:41:45.267053',14,13);
/*!40000 ALTER TABLE `workspace_register` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zzim`
--

DROP TABLE IF EXISTS `zzim`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zzim` (
  `zzim_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `course_id` bigint NOT NULL,
  `workspace_member_id` bigint NOT NULL,
  PRIMARY KEY (`zzim_id`),
  KEY `FK6gw71nwv5o70362lv8swvmfjf` (`course_id`),
  KEY `FKpattwv1io5tc4spi41bld87rh` (`workspace_member_id`),
  CONSTRAINT `FK6gw71nwv5o70362lv8swvmfjf` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `FKpattwv1io5tc4spi41bld87rh` FOREIGN KEY (`workspace_member_id`) REFERENCES `workspace_member` (`workspace_member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zzim`
--

LOCK TABLES `zzim` WRITE;
/*!40000 ALTER TABLE `zzim` DISABLE KEYS */;
INSERT INTO `zzim` VALUES (1,'2026-02-04 15:36:41.930179',3,6),(2,'2026-02-04 15:36:42.677282',1,6),(3,'2026-02-04 15:53:13.354465',2,6),(8,'2026-02-05 23:25:17.650214',4,10),(11,'2026-02-06 14:03:40.030551',4,3),(12,'2026-02-06 14:04:45.908780',7,16),(13,'2026-02-06 15:01:14.760138',1,10),(17,'2026-02-06 15:37:37.368882',2,10),(19,'2026-02-06 19:49:40.055485',7,17),(20,'2026-02-07 15:52:32.084083',5,19),(21,'2026-02-07 15:57:28.010849',4,19),(22,'2026-02-07 15:57:40.786775',3,19),(23,'2026-02-08 02:01:50.483196',9,9),(24,'2026-02-08 02:01:52.761355',8,9),(28,'2026-02-08 20:00:29.363527',7,14);
/*!40000 ALTER TABLE `zzim` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-09  9:08:18
