package ku.cse.team11.RankHub.domain.favorite;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional
    public Favorite addFavorite(Long memberId, Long contentId) {
        // 이미 존재하면 예외 발생 or 그냥 리턴
        if (favoriteRepository.existsByMemberIdAndContentId(memberId, contentId)) {
            throw new IllegalStateException("이미 즐겨찾기한 콘텐츠입니다.");
        }

        Favorite favorite = Favorite.builder()
                .memberId(memberId)
                .contentId(contentId)
                .build();

        return favoriteRepository.save(favorite);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long memberId, Long contentId) {
        return favoriteRepository.existsByMemberIdAndContentId(memberId, contentId);
    }

    @Transactional(readOnly = true)
    public List<Favorite> getFavoritesByMember(Long memberId) {
        return favoriteRepository.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public long countFavoritesByContent(Long contentId) {
        return favoriteRepository.countByContentId(contentId);
    }

    @Transactional
    public void removeFavorite(Long memberId, Long contentId) {
        if (!favoriteRepository.existsByMemberIdAndContentId(memberId, contentId)) {
            throw new IllegalStateException("즐겨찾기에 존재하지 않는 항목입니다.");
        }
        favoriteRepository.deleteByMemberIdAndContentId(memberId, contentId);
    }
}