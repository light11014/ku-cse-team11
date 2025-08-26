package ku.cse.team11.RankHub.domain.login;


import ku.cse.team11.RankHub.domain.member.Member;
import ku.cse.team11.RankHub.domain.member.MemberRepository;
import ku.cse.team11.RankHub.dto.auth.LoginRequest;
import ku.cse.team11.RankHub.dto.auth.LoginResponse;
import ku.cse.team11.RankHub.dto.auth.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long signup(SignupRequest req) {
        if (memberRepository.existsByLoginId(req.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 아이디입니다.");
        }
        Member m = new Member();
        m.setLoginId(req.loginId());
        m.setName(req.name());
        m.setPassword(req.password()); // ⚠️ 평문 그대로 저장
        return memberRepository.save(m).getId();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Member m = memberRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        if (!req.password().equals(m.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new LoginResponse(m.getId(), m.getLoginId(), m.getName(), "LOGIN_OK");
    }
}