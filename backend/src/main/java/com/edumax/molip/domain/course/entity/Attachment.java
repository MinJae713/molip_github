package com.edumax.molip.domain.course.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Attachment")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attachment_id", nullable = false)
  private Long attachmentId;

  @Column(nullable = false, length = 1000)
  private String url;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Enumerated(EnumType.STRING)
  @Column(name = "size_unit", nullable = false, length = 10)
  private AttachmentSizeUnit sizeUnit;

  public enum AttachmentSizeUnit {
    B, MB, KB, GB, TB
  }

  @Column(name = "upload_date", nullable = false)
  private LocalDateTime uploadDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "extension_type", nullable = false, length = 10)
  private AttachmentExtensionType extensionType;

  public enum AttachmentExtensionType {
    JPG, PNG, PDF, ZIP, DOCX
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AttachmentType type;

  public enum AttachmentType {
    COMMON, SESSION
  }

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}

