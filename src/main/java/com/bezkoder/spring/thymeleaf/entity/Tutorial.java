package com.bezkoder.spring.thymeleaf.entity;

import javax.persistence.*;

/*@
  @ invariant title != null && !title.isEmpty();
  @ invariant level >= 0 && level <= 10;
  @*/

/**
 * A tutorial entity with basic constraints:
 * - title is non-null and non-empty
 * - level is between 0 and 10
 */
@Entity
@Table(name = "tutorials")
public class Tutorial {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @Column(length = 128, nullable = false)
  private String title;

  @Column(length = 256)
  private String description;

  @Column(nullable = false)
  private int level;

  @Column
  private boolean published;

  public Tutorial() {

  }

  /*@
    @ requires title != null && !title.isEmpty();
    @ requires level >= 0 && level <= 10;
    @ ensures this.title.equals(title);
    @ ensures this.description == description;
    @ ensures this.level == level;
    @ ensures this.published == published;
    @*/
  public Tutorial(String title, String description, int level, boolean published) {
    this.title = title;
    this.description = description;
    this.level = level;
    this.published = published;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  /*@
    @ ensures \result == title;
    @*/
  public String getTitle() {
    return title;
  }

  /*@
    @ requires title != null && !title.isEmpty();
    @ ensures this.title.equals(title);
    @*/
  public void setTitle(String title) {
    this.title = title;
  }

  /*@
    @ ensures \result == description;
    @*/
  public String getDescription() {
    return description;
  }

  /*@
    @ ensures this.description == description;
    @*/
  public void setDescription(String description) {
    this.description = description;
  }

  /*@
    @ ensures \result == level;
    @*/
  public int getLevel() {
    return level;
  }

  /*@
    @ requires level >= 0 && level <= 10;
    @ ensures this.level == level;
    @*/
  public void setLevel(int level) {
    this.level = level;
  }

  /*@
    @ ensures \result == published;
    @*/
  public boolean isPublished() {
    return published;
  }

  /*@
    @ ensures this.published == published;
    @*/
  public void setPublished(boolean published) {
    this.published = published;
  }

  @Override
  public String toString() {
    return "Tutorial [id=" + id + ", title=" + title + ", description=" + description + ", level=" + level
            + ", published=" + published + "]";
  }

}
