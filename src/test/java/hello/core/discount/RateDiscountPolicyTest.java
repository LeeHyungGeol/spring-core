package hello.core.discount;

import hello.core.member.Grade;
import hello.core.member.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RateDiscountPolicyTest {
    RateDiscountPolicy discountPolicy = new RateDiscountPolicy();
    
    @Test
    @DisplayName("VIP는 할인이 적용되어야 한다.")
    public void vip_o() throws Exception {
        // given
        Member member = new Member(1L, "memberVIP", Grade.VIP);
        // when
        int discount = discountPolicy.discount(member, 10000);
        // then
        Assertions.assertEquals(discount, 1000);
    }

    @Test
    @DisplayName("BASIC이면 할인이 적용되지 않아야 한다.")
    public void vip_x() throws Exception {
        // given
        Member member = new Member(1L, "memberBASIC", Grade.BASIC);
        // when
        int discount = discountPolicy.discount(member, 10000);
        // then
        Assertions.assertEquals(discount, 0);
    }
}
