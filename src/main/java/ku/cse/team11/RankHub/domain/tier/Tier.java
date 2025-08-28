package ku.cse.team11.RankHub.domain.tier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Tier {
    S(50), A(40), B(30), C(20), D(10), F(0), None(-1);
    private final int score;
}
