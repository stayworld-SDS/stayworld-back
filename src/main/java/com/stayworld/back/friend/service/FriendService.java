package com.stayworld.back.friend.service;

import com.stayworld.back.friend.dto.DegreeResponse;
import com.stayworld.back.friend.dto.FriendDto;
import com.stayworld.back.friend.dto.PathNode;
import com.stayworld.back.friend.entity.Friend;
import com.stayworld.back.friend.exception.DuplicateFriendException;
import com.stayworld.back.friend.exception.FriendNotFoundException;
import com.stayworld.back.friend.repository.FriendRepository;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final KinshipCalculator kinshipCalculator;

    public List<FriendDto> getMyFriends(Long userId) {
        return getFriends(userId);
    }

    /** 로그인 유저 ↔ 대상 유저의 촌수와 경로. */
    public DegreeResponse getDegree(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("본인과의 촌수는 계산할 수 없습니다.");
        }
        if (!userRepository.existsById(targetUserId)) {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }

        Optional<List<Long>> path = kinshipCalculator.shortestPath(userId, targetUserId);
        if (path.isEmpty()) {
            return DegreeResponse.unreachable(targetUserId);
        }

        List<Long> ids = path.get();
        Map<Long, String> nicknameById = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        List<PathNode> nodes = ids.stream()
                .map(id -> new PathNode(id, nicknameById.get(id)))
                .toList();
        return DegreeResponse.reachable(targetUserId, nodes);
    }

    public List<FriendDto> getFriendsOf(Long targetUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }
        return getFriends(targetUserId);
    }

    private List<FriendDto> getFriends(Long userId) {
        List<Friend> friends = friendRepository.findByUserIdOrderByCreatedAtDesc(userId);

        Map<Long, String> nicknameById = userRepository.findAllById(
                        friends.stream().map(Friend::getFriendId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return friends.stream()
                .map(f -> toDto(f, nicknameById.get(f.getFriendId())))
                .toList();
    }

    @Transactional
    public FriendDto addFriend(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("본인은 일촌으로 추가할 수 없습니다.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        if (friendRepository.existsByUserIdAndFriendId(userId, targetUserId)) {
            throw new DuplicateFriendException();
        }

        Friend friend = Friend.builder()
                .userId(userId)
                .friendId(targetUserId)
                .build();
        friendRepository.save(friend);

        return toDto(friend, target.getNickname());
    }

    @Transactional
    public void removeFriend(Long userId, Long targetUserId) {
        Friend friend = friendRepository.findByUserIdAndFriendId(userId, targetUserId)
                .orElseThrow(FriendNotFoundException::new);
        friendRepository.delete(friend);
    }

    private FriendDto toDto(Friend friend, String nickname) {
        FriendDto dto = new FriendDto();
        dto.setUserId(friend.getFriendId());
        dto.setNickname(nickname);
        dto.setSince(friend.getCreatedAt());
        return dto;
    }
}
