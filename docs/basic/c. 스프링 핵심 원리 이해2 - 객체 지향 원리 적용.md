# 스프링 핵심 원리 #3

<br/>

# ✔ 목차
* 새로운 할인 정책 개발
* 관심사 분리
* 할인 정책 변경
* 좋은 객체 지향 설계의 5가지 원칙의 적용
* IoC, DI, 그리고 컨테이너
* 스프링으로 전환하기

<br/>

# 💡 새로운 할인 정책 개발

기존의 할인 정책(1000원 고정 할인)을 새로운 할인 정책(물건 가격의 10%)으로 바꾸려고 한다.

우리는 다형성을 이용해 객체지향 설계 원칙을 잘 준수했다고 생각하는데, 과연 잘 지켰는지 새로운 할인 정책을 추가하며 알아보자.

<br/>

## **RateDiscountPolicy 코드 추가**

```java
public class RateDiscountPolicy implements DiscountPolicy{

    private final int discountPercent = 10;

    @Override
    public int discount(Member member, int price) {
        if(member.getGrade() == Grade.VIP){
            return price * 10 / 100;
        }else {
            return 0;
        }
    }
}
```

<br/>

## **새로운 할인 정책 적용과 문제점**

방금 추가한 할인 정책을 적용하려면 클라이언트인 `OrderServiceImpl` 코드를 **고.쳐.야. 한다...?!**

```java
public class OrderServiceImpl implements OrderService {
    // private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
}
```

여기서 문제점을 발견할 수 있다.

* DIP 위반
    * OrderServiceImpl(클라이언트)는 DiscountPolicy 인터페이스(역)와 RateDiscountPolicy(구현 객체)를 **모두 의존하고 있다.**

* OCP 위반
    * 지금 코드는 기능을 확정하면, 위와 같이 **기존의 코드**를 **변.경. 해야한다.**

<br/>

# 💡 관심사 분리

우리는 다형성을 이용해 설계를 했는데도 DIP, OCP를 위반하였다.

**이 문제들은 관심사를 분리하여 해결할 수 있다.**

* 애플리케이션을 하나의 공연으로 생각

* 현재 코드는 OrderServiceImpl(클라이언트)가 의존하는 RateDiscountPolicy(구현 객체)를 직접 생성하고, 실행한다.

* 비유를 하면 기존에는 남자 주인공 배우가 공연도 하고, 동시에 여자 주인공도 직접 초빙하는 다양한 책임을 가지고 있다.

* 공연을 구성하고, 담당 배우를 섭외하고, 지정하는 책임을 담당하는 별도의 공연 기획자 AppConfig를 등장시킨다.

* AppConfig는 애플리케이션의 전체 동작 방식을 구성(config)하기 위해, 구현 객체를 생성하고, 연결하는
  책임을 부여한다.

<br/>

## **AppConfig**

```java
public class AppConfig {

    private DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }

    private MemberRepository MemberRepository() {
        return new MemoryMemberRepository();
    }

    public MemberServiceImpl memberService(){
        return new MemberServiceImpl(MemberRepository());
    }

    public OrderServiceImpl orderService(){
        return new OrderServiceImpl(MemberRepository(), discountPolicy());
    }
}
```

* AppConfig는 애플리케이션의 실제 동작에 필요한 구현 객체를 생성한다.

    * MemberServiceImpl
    * MemoryMemberRepository
    * OrderServiceImpl
    * FixDiscountPolicy

* AppConfig는 생성한 객체 인스턴스의 참조(레퍼런스)를 **생성자를 통해서 주입(연결)해준다.(생성자 주입)**

* discountPolicy 는 FixDiscountPolicy 정책을 사용한다는 역할과 구현을 한 눈에 볼 수 있게 정리해야한다.

<br/>

## **Service 구조 변경**

**OrderServiceImpl**

```java
public class OrderServiceImpl implements OrderService{

    private MemberRepository memberRepository;
    private DiscountPolicy discountPolicy;

    // 생성자 주입
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```

<br/>

**MemberServiceImpl**

```java
public class MemberServiceImpl implements MemberService{
    
    private MemberRepository memberRepository;

    // 생성자 주입
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

* 설계 변경으로 MemberServiceImpl과 OrderServiceImpl은 MemoryMemberRepository와 FixDiscountPolicy를 의존하지 않는다.

*  단지 MemberRepository, DiscountPolicy (인터페이스) 들만 의존한다.

* MemberServiceImpl의 생성자를 통해서 어떤 구현 객체를 주입할지는 오직 외부(AppConfig)에서 결정
  된다.

* MemberServiceImpl은 이제부터 의존관계에 대한 고민은 외부에 맡기고 실행에만 집중하면 된다.

<br/>

클래스 다이어그램으로 표현하면 다음과 같다.

![a](https://user-images.githubusercontent.com/55661631/106859705-bd666080-6706-11eb-87fb-5f91aab6cbad.PNG)

<br/>

# 💡 할인 정책 변경

처음으로 돌아가서 정액 할인 정책을 정률% 할인 정책으로 변경해보자.

어떤 부분만 변경하면 되겠는가?

AppConfig의 등장으로 애플리케이션이 크게 **사용 영역**과 **구성 영역**으로 분리되었다.

따라서 구성 영역의 구현 객체를 변경하기만 하면 된다.

<br/>

![b](https://user-images.githubusercontent.com/55661631/106860890-5a75c900-6708-11eb-8fb4-f00d11478537.PNG)

<br/>

위 그림의 과정을 코드로 나타내면 아래와 같다.

```java
public class AppConfig {

    private DiscountPolicy discountPolicy() {
        //return new FixDiscountPolicy();
        return new RateDiscountPolicy();
    }

    private MemberRepository MemberRepository() {
        return new MemoryMemberRepository();
    }

    public MemberServiceImpl memberService(){
        return new MemberServiceImpl(MemberRepository());
    }

    public OrderServiceImpl orderService(){
        return new OrderServiceImpl(MemberRepository(), discountPolicy());
    }
}
```

<br/>

## **정리**

* 객체의 생성과 연결은 AppConfig 가 담당한다.

* **DIP 완성** : MemberServiceImpl 은 MemberRepository 인 **추상에만 의존**하면 된다. 이제 구체 클래스를
  몰라도 된다.

* **관심사의 분리** : 객체를 생성하고 연결하는 역할과 실행하는 역할이 명확히 분리되었다.

* appConfig 객체는 memoryMemberRepository 객체를 생성하고 그 참조값을 memberServiceImpl 을
  생성하면서 **생성자로 전달한다(생성자 주입).**

* 클라이언트인 memberServiceImpl 입장에서 보면 의존관계를 마치 외부에서 주입해주는 것 같다고 해서
  DI(Dependency Injection) 우리말로 **의존관계 주입**이라 한다.

<br/>

# 💡 좋은 객체 지향 설계의 5가지 원칙의 적용

## **SRP 단일 책임 원칙**
한 클래스는 하나의 책임만 가져야 한다.

* 클라이언트 객체는 직접 구현 객체를 생성하고, 연결하고, 실행하는 다양한 책임을 가지고 있다.
* SRP 단일 책임 원칙을 따르면서 관심사를 분리했다.
* 구현 객체를 생성하고 연결하는 책임은 AppConfig가 담당한다.
* 클라이언트 객체는 실행하는 책임만 담당한다.

<br/>

## **DIP 의존관계 역전 원칙**
프로그래머는 "추상화에 의존해야지, 구체화에 의존하면 안된다." 의존성 주입은 이 원칙을 따르는 방법 중
하나다.

* 클라이언트 코드가 DiscountPolicy 추상화 인터페이스에만 의존하도록 코드를 변경했다.
* 하지만 클라이언트 코드는 인터페이스만으로는 아무것도 실행할 수 없다.
* AppConfig가 FixDiscountPolicy 객체 인스턴스를 클라이언트 코드 대신 생성해서 클라이언트 코드
  에 의존관계를 주입했다. 이렇게 해서 DIP 원칙을 따르면서 문제도 해결했다.

<br/>

## **OCP**
소프트웨어 요소는 확장에는 열려 있으나 변경에는 닫혀 있어야 한다

* 애플리케이션을 사용 영역과 구성 영역으로 나눴다.
* AppConfig가 의존관계를 FixDiscountPolicy RateDiscountPolicy 로 변경해서 클라이언트 코
  드에 주입하므로 클라이언트 코드는 변경하지 않아도 된다.
* 소프트웨어 요소를 새롭게 확장해도 사용 역영의 변경은 닫혀 있다.

<br/>

# 💡 IoC, DI, 그리고 컨테이너

## **제어의 역전 IoC(Inversion of Control)**

* **IoC 는 "어떻게" 가 아니라 "누가"**

* 기존 프로그램은 클라이언트 구현 객체가 스스로 필요한 서버 구현 객체를 생성하고, 연결하고, 실행했다. 한마디로 구현 객체가 프로그램의 제어 흐름을 스스로 조종했다. 개발자 입장에서는 자연스러운 흐름이다.

* 반면에 AppConfig가 등장한 이후에 구현 객체는 자신의 로직을 실행하는 역할만 담당한다. 프로그램의 제
  어 흐름은 이제 AppConfig가 가져갔다.

* 이렇듯 **개발자, 내가 객체의 생성, 사용, 연결등과 같은 제어 흐름을 직접 제어하는 것이 아니라 외부(AppConfig) (외부 환경, 외부 컨테이너, 프레임워크)에서 관리하는 것을 제어의 역전(IoC)** 이라 한다.

<br/>

## **프레임워크 vs 라이브러리**

* **제어의 역전(Ioc)가 일어나는지를 보고 판단한다.**

* 프레임워크가 개발자, 내가 작성한 코드를 제어하고, 대신 실행하면 그것은 프레임워크가 맞다.(JUnit)

* 반면에 내가 작성한 코드가 직접 제어의 흐름을 담당한다면 그것은 프레임워크가 아니라 라이브러리다.

<br/>

## **의존관계 주입 DI(Dependency Injection)**

* IoC 의 한 형태로 IoC 를 실제로 어떻게 적용할 것인가에 대한 하나의 해결책

* DI 는 "어떻게" 에 초점을 맞춘 것

* **객체간의 의존성(의존관계)을 코드 내부에서 직접 작성하는 것이 아닌 외부에서 주입받는 방식**

* 애플리케이션 **실행 시점(런타임)** 에 **외부에서** 실제 구현 객체를 생성하고 클라이언트에 전달해서 클라이언트와 서버의 실제 의존관계가 연결 되는 것을 **의존관계 주입**이라 한다.

* 객체 인스턴스를 생성하고, 그 참조값을 전달해서 연결된다.

* 의존관계 주입을 사용하면 클라이언트 코드를 변경하지 않고, 클라이언트가 호출하는 대상의 타입 인스턴스를 변경할 수 있다.

* 의존관계 주입을 사용하면 정적인 클래스 의존관계(클라이언트 코드)를 변경하지 않고, 동적인 객체 인스턴스 의존관계를 쉽게 변경할 수 있다.

<br/>

## **IoC 컨테이너, DI 컨테이너**

* AppConfig 처럼 객체를 생성하고 관리하면서 의존관계를 연결해 주는 것을 IoC 컨테이너 또는 DI 컨테이너라 한다.

* 의존관계 주입에 초점을 맞추어 최근에는 주로 DI 컨테이너라 한다. IoC 컨테이너는 너무 범용적인 말이다.

* 또는 어샘블러, 오브젝트 팩토리 등으로 불리기도 한다.

<br/>

# 💡 스프링으로 전환하기

지금까지는 순수한 자바 코드만으로 DI를 적용했다. 이제 스프링을 사용해보자.

<br/>

## **AppConfig 스프링 기반으로 변경**

```java
@Configuration
public class AppConfig {

    @Bean
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }

    @Bean
    public MemberRepository MemberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public MemberServiceImpl memberService(){
        return new MemberServiceImpl(MemberRepository());
    }

    @Bean
    public OrderServiceImpl orderService(){
        return new OrderServiceImpl(MemberRepository(), discountPolicy());
    }
}
```

* AppConfig에 설정을 구성한다는 뜻의 @Configuration 을 붙여준다.
* 각 메서드에 @Bean 을 붙여준다. 이렇게 하면 스프링 컨테이너에 스프링 빈으로 등록한다.

<br/>

## **스프링 컨테이너 적용**

**코드**
```java
public class MemberApp {
    public static void main(String[] args) {
      //        AppConfig appConfig = new AppConfig();
//        MemberService memberService = appConfig.memberService();

      ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
      MemberService memberService = ac.getBean("memberService", MemberService.class);
      Member member = new Member(1L, "memberA", Grade.VIP);
      memberService.join(member);

      Member findMember = memberService.findMember(1L);
      System.out.println("member.getName() = " + member.getName());
      System.out.println("findMember.getName() = " + findMember.getName());
    }
}
```

<br/>

**로그**
```java
[main] DEBUG o.s.c.a.AnnotationConfigApplicationContext -- Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@4f9a3314
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'org.springframework.context.annotation.internalConfigurationAnnotationProcessor'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'org.springframework.context.event.internalEventListenerProcessor'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'org.springframework.context.event.internalEventListenerFactory'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'org.springframework.context.annotation.internalAutowiredAnnotationProcessor'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'org.springframework.context.annotation.internalCommonAnnotationProcessor'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'appConfig'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'memberService'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'memberRepository'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'orderService'
[main] DEBUG o.s.b.f.s.DefaultListableBeanFactory -- Creating shared instance of singleton bean 'discountPolicy'        
```

* ApplicationContext 를 스프링 컨테이너라 한다.

* 기존에는 개발자가 AppConfig 를 사용해서 직접 객체를 생성하고 DI를 했지만, 이제부터는 스프링 컨테이너를 통해서 사용한다.

* 스프링 컨테이너는 @Configuration 이 붙은 AppConfig 를 설정(구성) 정보로 사용한다. 여기서 @Bean 이라 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록한다. 이렇게 스프링 컨테이너에 등록된 객체를 스프링 빈이라 한다.

* 스프링 빈은 @Bean 이 붙은 메서드의 명을 스프링 빈의 이름으로 사용한다.

* 이전에는 개발자가 필요한 객체를 AppConfig 를 사용해서 직접 조회했지만, 이제부터는 스프링 컨테이너를 통해서 필요한 스프링 빈(객체)를 찾아야 한다. 스프링 빈은 applicationContext.getBean() 메서드를 사용해서 찾을 수 있다.

* 기존에는 개발자가 직접 자바코드로 모든 것을 했다면 이제부터는 스프링 컨테이너에 객체를 스프링 빈으로 등록하고, 스프링 컨테이너에서 스프링 빈을 찾아서 사용하도록 변경되었다.

* 로그를 보면 스프링 컨테이너에 AppConfig, @Bean 이 적힌 메서드들이 차례대로 등록되는 것을 볼 수 있다.

<br/>

# 참고

* [인프런 스프링 핵심 원리](https://www.inflearn.com/)