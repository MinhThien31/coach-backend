package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.SubmissionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionCommentRepository
        extends JpaRepository<SubmissionComment, Long> {

    List<SubmissionComment> findBySubmissionId(Long submissionId);

}
