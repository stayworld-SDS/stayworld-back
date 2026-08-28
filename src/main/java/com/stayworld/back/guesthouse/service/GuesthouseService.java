package com.stayworld.back.guesthouse.service;

import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuesthouseService {
    private final GuesthouseRepository guesthouseRepository;
}
