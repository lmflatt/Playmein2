package com.theironyard.services;

import com.theironyard.entities.Loop;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

/**
 * Created by lee on 11/3/16.
 */
public interface LoopRepository extends CrudRepository<Loop, Integer> {
    ArrayList<Loop> findByGenreAndVoice(String genre, String voice);
}
