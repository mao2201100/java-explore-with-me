package ru.practicum.hit.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.hit.model.Hit;


@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {



}