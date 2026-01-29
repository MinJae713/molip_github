package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.Zzim;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZzimRepository extends JpaRepository<Zzim, Long> {
  Optional<Zzim> findByWorkspaceMemberAndCourse(WorkspaceMember workspaceMember, Course course);
}
