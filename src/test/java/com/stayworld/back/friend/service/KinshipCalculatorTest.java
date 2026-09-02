package com.stayworld.back.friend.service;

import com.stayworld.back.friend.entity.Friend;
import com.stayworld.back.friend.repository.FriendRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class KinshipCalculatorTest {

    /** 무방향 그래프: 1-2, 2-3, 3-4, 1-5, 5-4. (1-5 는 반대 방향 행 (user=5, friend=1) 으로만 존재) */
    private static final List<Friend> DEFAULT_GRAPH = List.of(
            edge(1, 2),
            edge(2, 3),
            edge(3, 4),
            edge(5, 1),
            edge(5, 4)
    );

    @Mock
    FriendRepository friendRepository;
    @InjectMocks
    KinshipCalculator calculator;

    @BeforeEach
    void setUp() {
        stubGraph(DEFAULT_GRAPH);
    }

    @Test
    void 자기_자신은_0촌() {
        assertThat(calculator.shortestPath(1L, 1L)).contains(List.of(1L));
    }

    @Test
    void 직접_일촌은_1촌() {
        assertThat(calculator.shortestPath(1L, 2L)).contains(List.of(1L, 2L));
    }

    @Test
    void 반대_방향으로_저장된_행도_간선으로_인정된다() {
        // 1-5 는 (user=5, friend=1) 행으로만 존재
        assertThat(calculator.shortestPath(1L, 5L)).contains(List.of(1L, 5L));
    }

    @Test
    void 더_짧은_경로를_고른다() {
        // 1-5-4 (2촌) vs 1-2-3-4 (3촌)
        assertThat(calculator.shortestPath(1L, 4L)).contains(List.of(1L, 5L, 4L));
    }

    @Test
    void 도달_불가하면_empty() {
        assertThat(calculator.shortestPath(1L, 99L)).isEmpty();
    }

    @Test
    void degrees_여러_대상을_한_번의_BFS로_계산한다() {
        Map<Long, Integer> degrees = calculator.degrees(1L, Set.of(1L, 2L, 3L, 4L, 99L));

        assertThat(degrees)
                .containsEntry(1L, 0)
                .containsEntry(2L, 1)
                .containsEntry(3L, 2)
                .containsEntry(4L, 2)
                .doesNotContainKey(99L);
    }

    @Test
    void MAX_DEPTH_를_넘는_거리는_도달_불가로_처리한다() {
        // 1-2-3-4-5-6-7-8 체인: 1→8 은 7촌 > MAX_DEPTH(6), 1→7 은 6촌으로 도달 가능
        stubGraph(List.of(
                edge(1, 2), edge(2, 3), edge(3, 4), edge(4, 5),
                edge(5, 6), edge(6, 7), edge(7, 8)
        ));

        assertThat(calculator.shortestPath(1L, 7L))
                .contains(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        assertThat(calculator.shortestPath(1L, 8L)).isEmpty();
    }

    private void stubGraph(List<Friend> graph) {
        lenient().when(friendRepository.findByUserIdIn(anyCollection())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return graph.stream().filter(f -> ids.contains(f.getUserId())).toList();
        });
        lenient().when(friendRepository.findByFriendIdIn(anyCollection())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return graph.stream().filter(f -> ids.contains(f.getFriendId())).toList();
        });
    }

    private static Friend edge(long userId, long friendId) {
        return Friend.builder().userId(userId).friendId(friendId).build();
    }
}
