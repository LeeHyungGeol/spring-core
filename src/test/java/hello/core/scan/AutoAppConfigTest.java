package hello.core.scan;

import static org.junit.jupiter.api.Assertions.*;

import hello.core.AutoAppConfig;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class AutoAppConfigTest {

    @Test
    void basicScan() throws Exception {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(
            AutoAppConfig.class);

        MemberService memberService = ac.getBean("memberServiceImpl", MemberService.class);
        Assertions.assertThat(memberService).isInstanceOf(MemberService.class);
    }

}