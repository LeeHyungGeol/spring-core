# 스프링 핵심 원리 #9

<br/>

# ✔ 목차
* 빈 스코프란?
* 프로토타입 스코프
* 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 문제점
* 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 Provider로 문제 해결
* 웹 스코프
* request 스코프 예제 만들기
* 스코프와 Provider
* 스코프와 프록시

<br/>

<br/>

# 💡 빈 스코프란?

지금까지 우리는 스프링 빈이 스프링 컨테이너의 시작과 함께 생성되어서 스프링 컨테이너가 종료될 때 까지 유지된다고 학습했다.

이것은 스프링 빈이 기본적으로 싱글톤 스코프로 생성되기 때문이다. 스코프는 번역 그대로 빈이 존재할 수 있는 범위를 뜻한다.

스프링은 다음과 같은 다양한 스코프를 지원한다.

* **싱글톤** : 기본 스코프, 스프링 컨테이너의 시작과 종료까지 유지되는 가장 넓은 범위의 스코프이다.

* **프로토타입** : 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입까지만 관여하고 더는 관리하지 않는다. 또한 요청마다 서로 다른 인스턴스를 생성한다.
* **웹 관련 스코프**
    * **request** : 웹 요청이 들어오고 나갈때 까지 유지되는 스코프이다. 각 요청마다 서로 다른 인스턴스를 생성한다.
    * session : 웹 세션이 생성되고 종료될 때 까지 유지되는 스코프이다.
    * application : 웹의 서블릿 컨텍스와 같은 범위로 유지되는 스코프이다.

<br/>

<br/>

빈 스코프틑 다음과 같이 지정할 수 있다.

<br/>

### **컴포넌트 스캔 자동 등록**

```java
@Scope("prototype")
@Component
public class Bean {}
```

<br/>

### **수동 등록**

```java
@Scope("request")
@Component
PrototypeBean Bean() {
    return new Bean();
}
```

<br/>

<br/>

<br/>

# 💡 프로토타입 스코프

* 싱글톤 스코프 빈을 조회하면 스프링 컨테이너는 항상 같은 인스턴스의 스프링 빈을 반환한다.

* 반면에 프로토타입 스코프를 스프링 컨테이너에서 조회하면 스프링 컨테이너는 **항상 새로운 인스턴스**를 반환한다.

* 스프링 컨테이너는 프로토타입 빈을 생성하고, 의존관계 주입, 초기화까지만 처리하며 그 이후에는 관리하지 않는다.

* 따라서 프로토타입 빈을 관리할 책임은 프로토타입 빈을 받은 클라이언트에 있다. **그래서 `@PreDestroy` 같은 종료 메서드가 호출되지 않는다.**

<br/>

### **프로토타입 스코프 빈 테스트**

```java
void singletonBeanTest() {
        AnnotationConfigApplicationContext ac = 
                new AnnotationConfigApplicationContext(PrototypeBean.class);

        System.out.println("find bean1");
        PrototypeBean bean1 = ac.getBean(PrototypeBean.class);

        System.out.println("find bean2");
        PrototypeBean bean2 = ac.getBean(PrototypeBean.class);

        System.out.println("bean1 : " + bean1);
        System.out.println("bean2 : " + bean2);

        Assertions.assertThat(bean1).isNotSameAs(bean2);

        bean1.destroy();
        bean2.destroy();

        ac.close();
    }

    @Scope("prototype")
    static class PrototypeBean{

        @PostConstruct
        public void init(){
            System.out.println("PrototypeBean.init");
        }

        @PreDestroy
        public void destroy(){
            System.out.println("PrototypeBean.destroy");
        }
    }
```

<br/>

### **실행 결과**

```java
find bean1
PrototypeBean.init
find bean2
PrototypeBean.init
bean1 : dev.highright96.core.scope.PrototypeTest$PrototypeBean@1efe439d
bean2 : dev.highright96.core.scope.PrototypeTest$PrototypeBean@be68757
PrototypeBean.destroy
PrototypeBean.destroy
```

* 실행 결과를 보면 프로토타입 빈은 스프링 컨테이너 생성 시점이 아닌 조회 시점에 생성되고, 초기화 메서드도 실행된다.

* 프로토타입 빈은 스프링 컨테이너가 생성과 의존관계 주입 그리고 초기화 까지만 관여하고, 더는 관리하지 않는다.

* 따라서 프로토타입 빈은 스프링 컨테이너가 종료될 때 ``@PreDestory`` 같은 종료 메서드가 전혀 실행되지 않는다. 위의 결과는 직접 destroy 메서드를 호출한 결과이다.

<br/>

<br/>

### **정리**

* 스프링 컨테이너에 요청할 때 마다 새로 생성된다.

* 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입 그리고 초기화까지만 관여한다.

* 종료 메서드가 호출되지 않는다.

* **그래서 프로토타입 빈은 프로토타입 빈을 조회한 클라이언트가 관리해야 한다.**

* **종료 메서드에 대한 호출도 클라이언트가 직접 해야한다.**


<br/>

<br/>

<br/>

# 💡 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 문제점

스프링은 일반적으로 싱글톤 빈을 사용하므로, 싱글톤 빈이 프로토타입 빈을 사용하게 된다.

그런데 싱글톤 빈은 생성 시점에만 의존관계 주입을 받기 때문에, 프로토타입 빈이 새로 생성되기는 하지만, 싱글톤 빈과 함께 계속 유지되는 것이 문제다.

<br/>

아래 예제를 보자.

<br/>

![a](https://user-images.githubusercontent.com/55661631/109278063-72e68880-785b-11eb-8cb5-c676bb765846.PNG)

클라이언트A 와 클라이언트B가 싱글톤 빈과 함께 사용되는 프로토타입 빈의 addCount() 메서드를 호출하는 예제이다.

우리가 원하는 결과는 A, B 모두 count=1 이다. 하지만 결과는 클라이언트A는 1 클라이언트B는 2가 저장된다.

그 이유는 싱글톤 빈이 내부에 가지고 있는 프로토타입 빈은 이미 과거(싱글톤 빈이 생성될때)에 주입이 끝난 빈이기 때문에 이후에 새로 생성될 일이 없기 때문이다. **따라서 프로토타입 빈의 특징을 잃어버린다.**

<br/>

<br/>

<br/>

# 💡 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 Provider로 문제 해결

싱글톤 빈과 프로토타입 빈을 함께 사용할 때, 어떻게 하면 사용할 때 마다 항상 새로운 프로토타입 빈을 생성할 수 있을까?

필요한 의존관계를 찾는 Dependency Lookup(DL) 의존관계 조회를 제공하는 무언가를 사용하면 된다.

<br/>

<br/>

## **ObjectFactory, ObjectProvider**

지정된 빈을 컨테이너에서 대신 찾아주는 DL 서비스를 제공하는 것이 바로 `ObjectProvider`이다. 참고로 과거에는 `ObjectFactory`가 있었는데, 여기에 기능을 추가한 것이 `ObjectProvider`이다.

<br/>

<br/>

### **사용 예제**
```java
@Autowired
private ObjectProvider<PrototypeBean> prototypeBeanProvider;
    
public int logic() {
    PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
    prototypeBean.addCount();
    int count = prototypeBean.getCount();
    return count;
}
```

* 실행해보면 prototypeBeanProvider.getObject() 을 통해서 항상 새로운 프로토타입 빈이 생성되는 것을 확인할 수 있다.

* ObjectProvider 의 getObject() 를 호출하면 내부에서는 스프링 컨테이너를 통해 해당 빈을 찾아서 반환한다. (DL)

* 스프링이 제공하는 기능을 사용하지만, 기능이 단순하므로 단위테스트를 만들거나 mock 코드를 만들기는 훨씬 쉬워진다.

* ObjectProvider 는 지금 딱 필요한 DL 정도의 기능만 제공

<br/>

<br/>

### **특징**

* ObjectFactory : 기능이 단순, 별도의 라이브러리 필요 없음, 스프링에 의존

* ObjectProvider : ObjectFactory 상속, 옵션, 스트림 처리등 편의 기능이 많고, 별도의 라이브러리 필요 없음, 스프링에 의존

<br/>

<br/>

## **정리**

* 그러면 프로토타입 빈을 언제 사용할까? 매번 사용할 때 마다 의존관계 주입이 완료된 새로운 객체가 필요하면 사용하면 된다.

* **그런데 실무에서 웹 애플리케이션을 개발해보면, 싱글톤 빈으로 대부분의 문제를 해결할 수 있기 때문에 프로토타입 빈을 직접적으로 사용하는 일은 매우 드물다.**

* ObjectProvider , JSR330 Provider 등은 프로토타입 뿐만 아니라 DL이 필요한 경우는 언제든지 사용할 수 있다.

<br/>

<br/>

<br/>

# 💡 웹 스코프

지금까지 싱글톤과 프로토타입 스코프를 학습했다.
싱글톤은 스프링 컨테이너의 시작과 끝까지 함께하는 매우 긴 스코프이고, 프로토타입은 생성과 의존관계 주입, 그리고 초기화까지만 진행하는 특별한 스코프이다.

<br/>

## **웹 스코프의 특징**
- 웹 스코프는 웹 환경에서만 동작한다.
- 웹 스코프는 프로토타입과 다르게 **스프링이 해당 스코프의 종료시점까지 관리한다. 따라서 종료 메서드가 호출된다.**

<br/>

## **웹 스코프 종류**
- request: **HTTP 요청 *하나***가 들어오고 나갈 때 까지 유지되는 스코프, 각각의 HTTP 요청마다 별도의 빈 인스턴스가 생성되고, 관리된다.
- session: HTTP Session과 동일한 생명주기를 가지는 스코프
- application: 서블릿 컨텍스트( ServletContext )와 동일한 생명주기를 가지는 스코프
- websocket: 웹 소켓과 동일한 생명주기를 가지는 스코프

<br/>

### **HTTP request 요청 당 각각 할당되는 request 스코프**

![image](https://user-images.githubusercontent.com/83503188/200118403-37d66a57-43f2-40cc-8fec-c62ddec1c780.png)
- 클라이언트 A,B에 다른 스프링 빈이 생성되어 사용된다.

<br/>

<br/>

<br/>


# 💡 request 스코프 예제 만들기

동시에 여러 HTTP 요청이 오면 **정확히 어떤 요청이 남긴 로그인지 구분하기 어렵다.**

이럴때 사용하기 딱 좋은것이 바로 **request 스코프**이다.

<br/>

다음과 같이 로그가 남도록 request 스코프를 활용해서 추가 기능을 개발해보자
```text
[d06b992f...] request scope bean create
[d06b992f...][http://localhost:8080/log-demo] controller test
[d06b992f...][http://localhost:8080/log-demo] service id = testId
[d06b992f...] request scope bean close
```
- 기대하는 공통 포멧: [UUID][requestURL] {message}
- UUID를 사용해서 HTTP 요청을 구분하자.
- requestURL 정보도 추가로 넣어서 어떤 URL을 요청해서 남은 로그인지 확인하자.

<br/>

<br/>

### **MyLogger class**

```java
@Component
@Scope(value = "request")
public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
    }

    @PostConstruct
    public void init() {
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create:" + this);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[" + uuid + "] request scope bean close:" + this);
    }
}

```

- 로그를 출력하기 위한 MyLogger 클래스이다.
- `@Scope(value = "request")` 를 사용해서 request 스코프로 지정했다. 이제 이 빈은 HTTP 요청 당 하나씩 생성되고, HTTP 요청이 끝나는 시점에 소멸된다.
- 이 빈이 생성되는 시점에 자동으로 `@PostConstruct` 초기화 메서드를 사용해서 uuid를 생성해서 저장해둔다. 이 빈은 HTTP 요청 당 하나씩 생성되므로, uuid를 저장해두면 다른 HTTP 요청과 구분할 수 있다.
- 이 빈이 소멸되는 시점에 `@PreDestroy` 를 사용해서 종료 메시지를 남긴다.
- requestURL 은 이 빈이 생성되는 시점에는 알 수 없으므로, 외부에서 setter로 입력 받는다.

<br/>

<br/>

### **LogDemoController**

```java
@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    private final MyLogger myLogger;

    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURI();
        myLogger.setRequestURL(requestURL);

        myLogger.log("controller test");
        logDemoService.logic("testId");

        return "OK";
    }

}
```

- 로거가 잘 작동하는지 확인하는 테스트용 컨트롤러다.
- 여기서 HttpServletRequest를 통해서 요청 URL을 받았다.
  - requestURL 값 http://localhost:8080/log-demo
- 이렇게 받은 requestURL 값을 myLogger에 저장해둔다.
- **myLogger는 HTTP 요청 당 각각 구분되므로 다른 HTTP 요청 때문에 값이 섞이는 걱정은 하지 않아도 된다.**
- 컨트롤러에서 controller test라는 로그를 남긴다.

<br/>

> ### **참고** 
> requestURL을 MyLogger에 저장하는 부분은 컨트롤러 보다는 공통 처리가 가능한 스프링
인터셉터나 서블릿 필터 같은 곳을 활용하는 것이 좋다. 여기서는 예제를 단순화하고, 아직 스프링
인터셉터를 학습하지 않은 분들을 위해서 컨트롤러를 사용했다. 스프링 웹에 익숙하다면 인터셉터를
사용해서 구현해보자.

<br/>

<br/>

### **LogDemoService 추가**

```java
@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final MyLogger myLogger;

    public void logic(String id) {
        myLogger.log("service id = " + id);
    }
}

```

- 비즈니스 로직이 있는 서비스 계층에서도 로그를 출력해보자.
- 여기서 중요한점이 있다. request scope를 사용하지 않고 파라미터로 이 모든 정보를 서비스 계층에 넘긴다면, 파라미터가 많아서 지저분해진다. 더 문제는 requestURL 같은 웹과 관련된 정보가 웹과 관련없는 서비스 계층까지 넘어가게 된다.
  웹과 관련된 부분은 컨트롤러까지만 사용해야 한다. 서비스 계층은 웹 기술에 종속되지 않고, 가급적 순수하게 유지하는 것이 유지보수 관점에서 좋다.
- request scope의 MyLogger 덕분에 이런 부분을 파라미터로 넘기지 않고, MyLogger의 멤버변수에 저장해서 코드와 계층을 깔끔하게 유지할 수 있다.

<br/>

### **실행시 오류 발생!**

```text
Error creating bean with name 'myLogger': Scope 'request' is not active for the
current thread; consider defining a scoped proxy for this bean if you intend to
refer to it from a singleton;
```

스프링 애플리케이션을 실행하는 시점에 싱글톤 빈은 생성해서 주입이 가능하지만, request 스코프 빈은 아직 생성되지 않는다. 이 빈은 실제 고객의 요청이 와야 생성할 수 있다!

<br/>

<br/>

<br/>


# 💡 스코프와 Provider

첫번째 해결방안은 앞서 배운 **Provider**를 사용하는 것이다.

<br/>

<br/>

### **LogDemoController**

```java
@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    private final ObjectProvider<MyLogger> myLoggerProvider;

    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURI();
        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.setRequestURL(requestURL);

        myLogger.log("controller test");
        logDemoService.logic("testId");

        return "OK";
    }

}

```

### **LogDemoService**

```java
@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final ObjectProvider<MyLogger> myLoggerProvider;

    public void logic(String id) {
        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.log("service id = " + id);
    }
}
```

### **결과**

```text
[038eb5b4-140a-44b6-b196-12e78282a3ac] request scope bean create:hello.core.common.MyLogger@7b9e8c7
[038eb5b4-140a-44b6-b196-12e78282a3ac][http://localhost:8080/log-demo] controller test
[038eb5b4-140a-44b6-b196-12e78282a3ac][http://localhost:8080/log-demo] service id = testId
[038eb5b4-140a-44b6-b196-12e78282a3ac] request scope bean close:hello.core.common.MyLogger@7b9e8c7

[a3e83317-7e61-41f9-8595-cbed1f611895] request scope bean create:hello.core.common.MyLogger@3171d7d4
[a3e83317-7e61-41f9-8595-cbed1f611895][http://localhost:8080/log-demo] controller test
[a3e83317-7e61-41f9-8595-cbed1f611895][http://localhost:8080/log-demo] service id = testId
[a3e83317-7e61-41f9-8595-cbed1f611895] request scope bean close:hello.core.common.MyLogger@3171d7d4

[a0ce3fed-184d-4416-ba55-2fd8b1f2a557] request scope bean create:hello.core.common.MyLogger@769ff0c8
[a0ce3fed-184d-4416-ba55-2fd8b1f2a557][http://localhost:8080/log-demo] controller test
[a0ce3fed-184d-4416-ba55-2fd8b1f2a557][http://localhost:8080/log-demo] service id = testId
[a0ce3fed-184d-4416-ba55-2fd8b1f2a557] request scope bean close:hello.core.common.MyLogger@769ff0c8

[c34f4b26-c821-4f9c-a210-26938da9d69d] request scope bean create:hello.core.common.MyLogger@14a26b0e
[c34f4b26-c821-4f9c-a210-26938da9d69d][http://localhost:8080/log-demo] controller test
[c34f4b26-c821-4f9c-a210-26938da9d69d][http://localhost:8080/log-demo] service id = testId
[c34f4b26-c821-4f9c-a210-26938da9d69d] request scope bean close:hello.core.common.MyLogger@14a26b0e

```

<br/>

<br/>


## **정리**

- 매 요청마다 다른 uuid를 확인할 수 있다.
- ObjectProvider 덕분에 `ObjectProvider.getObject()` 를 호출하는 시점까지 request scope 빈의 생성을 지연할 수 있다.
- `ObjectProvider.getObject()` 를 호출하시는 시점에는 HTTP 요청이 진행중이므로 request scope 빈의 생성이 정상 처리된다.
- `ObjectProvider.getObject()` 를 LogDemoController , LogDemoService 에서 각각 한번씩 따로 호출해도 같은 HTTP 요청이면 같은 스프링 빈이 반환된다!

<br/>

<br/>

<br/>


# 💡 스코프와 프록시

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger { }
```

여기가 핵심이다. proxyMode = ScopedProxyMode.TARGET_CLASS 를 추가해주자.

적용 대상이 인터페이스가 아닌 **클래스**면 `TARGET_CLASS` 를 선택, 적용 대상이 **인터페이스**면 `INTERFACES` 를 선택

이렇게 하면 MyLogger의 가짜 프록시 클래스를 만들어두고 HTTP request와 상관 없이 가짜 프록시
클래스를 다른 빈에 미리 주입해 둘 수 있다.

<br/>

```java
@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    private final MyLogger myLogger;
//    private final ObjectProvider<MyLogger> myLoggerProvider;

    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
//        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.setRequestURL(requestURL);

        myLogger.log("controller test");
        logDemoService.logic("testId");

        return "OK";
    }

}
```


```java
@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final MyLogger myLogger;
//    private final ObjectProvider<MyLogger> myLoggerProvider;

    public void logic(String id) {
//        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.log("service id = " + id);
    }
}

```

## 웹 스코프와 프록시 동작 원리

먼저 주입된 myLogger를 확인해보자.

```java
myLogger = class hello.core.common.MyLogger$$SpringCGLIB$$0
```

<br/>


## **CGLIB라는 라이브러리로 내 클래스를 상속 받은 가짜 프록시 객체를 만들어서 주입한다.**

- `@Scope` 의 `proxyMode = ScopedProxyMode.TARGET_CLASS` 를 설정하면 스프링 컨테이너는 CGLIB 라는 바이트코드를 조작하는 라이브러리를 사용해서, MyLogger를 상속받은 가짜 프록시 객체를 생성한다.
- 결과를 확인해보면 우리가 등록한 순수한 MyLogger 클래스가 아니라 MyLogger$ `$EnhancerBySpringCGLIB` 이라는 클래스로 만들어진 객체가 대신 등록된 것을 확인할 수 있다.
- 그리고 스프링 컨테이너에 "myLogger"라는 이름으로 진짜 대신에 이 가짜 프록시 객체를 등록한다.
- `ac.getBean("myLogger", MyLogger.class)` 로 조회해도 프록시 객체가 조회되는 것을 확인할 수 있다.
- 그래서 의존관계 주입도 이 가짜 프록시 객체가 주입된다.

![image](https://user-images.githubusercontent.com/83503188/200119890-81675d1e-0f7a-4132-a924-194b6f5e68de.png)

<br/>


## **가짜 프록시 객체는 요청이 오면 그때 내부에서 진짜 빈을 요청하는 위임 로직이 들어있다.**

- 가짜 프록시 객체는 내부에 진짜 myLogger를 찾는 방법을 알고 있다.
- 클라이언트가 `myLogger.logic()` 을 호출하면 사실은 가짜 프록시 객체의 메서드를 호출한 것이다.
- 가짜 프록시 객체는 request 스코프의 진짜 `myLogger.logic()` 를 호출한다.
- 가짜 프록시 객체는 원본 클래스를 상속 받아서 만들어졌기 때문에 이 객체를 사용하는 클라이언트 입장에서는 사실 원본인지 아닌지도 모르게, 동일하게 사용할 수 있다(다형성)

<br/>

## **동작 정리**

- CGLIB라는 라이브러리로 내 클래스를 상속 받은 가짜 프록시 객체를 만들어서 주입한다.
- 이 가짜 프록시 객체는 실제 요청이 오면 그때 내부에서 실제 빈을 요청하는 위임 로직이 들어있다.
- 가짜 프록시 객체는 실제 request scope와는 관계가 없다. 그냥 가짜이고, 내부에 단순한 위임 로직만 있고, 싱글톤 처럼 동작한다.

<br/>


## **특징 정리**

- 프록시 객체 덕분에 클라이언트는 마치 싱글톤 빈을 사용하듯이 편리하게 request scope를 사용할 수 있다.
- **사실 Provider를 사용하든, 프록시를 사용하든 핵심 아이디어는 진짜 객체 조회를 꼭 필요한 시점까지 지연처리 한다는 점이다.**
  - 이게 중요하다!!!
- 단지 **애노테이션 설정 변경만**으로 원본 객체를 프록시 객체로 대체할 수 있다. **이것이 바로 다형성과 DI 컨테이너가 가진 큰 강점이다.**
- 꼭 웹 스코프가 아니어도 프록시는 사용할 수 있다.

<br/>


## **주의점**

- **마치 싱글톤을 사용하는 것 같지만 다르게 동작하기 때문에 결국 주의해서 사용해야 한다.**
- 이런 특별한 scope는 꼭 필요한 곳에만 최소화해서 사용하자, 무분별하게 사용하면 유지보수하기 어려워진다.

<br/>

<br/>

# ⭐️ 실무에서는 어떤 용도로 request scope를 가장 많이 사용하나요??

request scope 자체가 HTTP 요청 정보의 내용을 편리하게 다룰 수 있기 때문에, 이 요청 정보를 공통화해서 객체로 만들어두고, 공통으로 로그 처리를 하거나 또는 외부 API를 호출할 때 요청서버에서 넘어온 정보를 함께 넘기거나 할 때 유용하게 사용할 수 있습니다.

예를 들어서 요청서버 -> 현재서버 -> 대상서버

구조가 되어있을 때 요청서버에서 뭔가 요청 id를 만들어서 현재서버에 넘겼을 때 현재서버는 단순이 이 요청 id가 비즈니스 로직과는 전혀 상관이 없고, 로그용으로 필요하고, 또 대상 서버로 넘길 때 필요하다면 파라미터로 계속 가지고 다니기에는 부담스럽습니다. 이런 경우에 사용하면 비즈니스 로직을 전혀 손대지 않고, 공통 로그 처리, 외부 AP에I 파라미터 전달 등등 업무를 처리할 수 있습니다.

정리하면 request scope는 비즈니스 로직에 파라미터를 계속 들고다닐 필요 없이 공통 정보를 처리할 때 효과적입니다.

그런데 request scope 같은 것을 너무 자주 사용하면, http 요청이 없는 테스트 코드 등에서 고민할 내용이 많아지는 단점이 있습니다.


<br/>

# ⭐️싱글톤처럼 사용 시 문제가 될 수 있다는 것의 간단한 예

(상태를 가지지 않는) 싱글톤을 사용하는 이유 중 하나는 멀티스레드 환경에서 객체 재사용성을 높이기 위함입니다. 억지스러울 수 있지만 예를 들어보자면 엄청나게 많은 요청이 쏟아질 때 싱글톤처럼 보이는 프록시 객체를 호출했습니다. 그런데 프록시 객체가 뒤에서 호출한 건 프로토타입 스코프를 가지는 빈이었던 겁니다. 그러면 수많은 빈이 생성될테고 이는 서버에 불필요한 부하를 주게 될 것입니다. 원래 의도는 싱글톤 빈인 줄 알고 열심히 호출했었던 것이니깐요.

<br/>

# ⭐️ 혹시 이런 프록시 모드로 생성되는 객체는 스프링 컨테이너가 생성될 때, 단 한번 생성된다고 이해를 하면 될까요?

=> 네, 프록시 객체는 한 번만 생성됩니다.
