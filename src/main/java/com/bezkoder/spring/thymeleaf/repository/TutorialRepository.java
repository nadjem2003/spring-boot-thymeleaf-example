package com.bezkoder.spring.thymeleaf.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;

@Repository
@Transactional
public interface TutorialRepository extends JpaRepository<Tutorial, Integer> {
  List<Tutorial> findByTitleContainingIgnoreCase(String keyword);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE Tutorial t SET t.published = :published WHERE t.id = :id")
  void updatePublishedStatus(@Param("id") Integer id, @Param("published") boolean published);

}