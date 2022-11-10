https://github.com/back-end-study/effective-java/pull/61

## 람다는 직렬화 해서는 안되는 이유

> 람다도 익명 클래스처럼 직렬화 형태가 구현별로(가령 가상머신별로) 다를 수 있다. 따라서 람다를 직렬화하는 일은 극히 삼가야 한다(익명 클래스의 인스턴스도 마찬가지다)  - p.285 -
>

[오라클 공식문서](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html#serialization)에서도 람다 표현식과 익명 클래스에 대한 직렬화를 '강력하게' 권장하지 않다

그 이유는 람다 표현식이나 익명 클래스에 대해서 직렬화를 할 때, 자바 컴파일러가 특정 구문에 대해서 [Synthetic constructs](https://www.baeldung.com/java-synthetic)(인조 구문이 맞는 번역 같음)을 삽입하는데 이 Synthetic constructs는 컴파일러의 구현 방식에 따라서 다를 수 있다.
이 의미는 '.class' 파일도 컴파일러 구현에 따라 다를 수 있음을 의미한다. [내용 보기](https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html#serialization)

결과적으로 위의 이유에 의해서 다른 컴파일러를 사용하고 있는 경우 호환성의 문제가 발생할 수 있기 때문에 직렬화를 하는것은 강하게 권장하지 않는다!

---

## 람다를 직렬화 하는 방법

하지만 때에 따라서 람나 표현식이나 익명 클래스가 포함된 클래스를 직렬화를 해야할 때도 있을 수 있다

먼저, 람다가 정의된 클래스를 냅다 직렬화/역직렬화를 해보자
![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_15 11 27](https://user-images.githubusercontent.com/68587990/201023365-a8dfed5b-9bb4-4c03-a38e-fe8960f4bb88.png)


main 메소드에서 테스트 방법은 다음과 같다

1. Runnable 객체 fn 획득
2. fn 실행
3. fn 직렬화 후 파일로 저장
4. 파일 역직렬화
5. 역직렬화 된 객체 deserializedFn 실행

main 메소드를 실행 해보면
![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_14 57 54](https://user-images.githubusercontent.com/68587990/201023435-9da02cc7-10f7-4d58-8e24-52a7c675f4d8.png)


직렬화를 하려고 할 때 **`NotSerializableException`**이 발생하면서 직렬화에 실패한다..

### 람다 직렬화를 하면 안된다고 했지,, 하지 못한다고는 안 했잖아,,

class 파일을 봐도 별 문제가 없어보이는데..

![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_15 17 07](https://user-images.githubusercontent.com/68587990/201023485-a8b4cd5d-ff2a-4be3-81ff-f2d0cf62ce0c.png)


그렇다,,,! 익명 클래스의 생성,동작 방식은 컴파일 타임에 결정되지만, 람다는 런타임에 실제 호출시 결정된다고 한다. (**invokedynamic**)

그렇기 때문에 java를 실행 시킬 때 `-Djdk.internal.lambda.dumpProxyClasses={output-dir}` 옵션을 넣어주게 되면 실제 람다가 생성되는 클래스 파일을 볼 수 있다.

> ❗ `-Djdk.internal.lambda.dumpProxyClasses` 옵션 사용 시 같이 사용되고 있는 모든 라이브러리에 대한 class 파일이 생성될 수 있다
>

![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_15 33 10](https://user-images.githubusercontent.com/68587990/201023638-1539acbf-7eb4-4e9c-8157-22a40357aebb.png)


우리가 일반적으로 직렬화를 할 때 하는 방식으로 NotSerializableLambdaExpression 클래스에 Serializable 인터페이스를 선언했지만,, **위 옵션으로 생성된 람다의 class 파일을 보면 Serializable 인터페이스를 구현하고 있지 않다**..

### 람다를 직렬화 하려면 타입 캐스트(Type cast)를 해주어야 한다

우리가 선언한 **함수형 인터페이스와 Serializeble 인터페이스를 결합한 타입으로 람다식을 타입 캐스팅을 해야된다**

예를들어 우리가 사용하고 있는 함수형 인터페이스는 Runnable이고 이를 타입 캐스팅하면 아래와 같다
![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_15 42 02](https://user-images.githubusercontent.com/68587990/201023704-19370f26-9119-4e15-ad96-2934ee4710ab.png)



타입 캐스팅을 해준 뒤 생성된 람다 class 파일을 살펴보면
![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_15 44 39](https://user-images.githubusercontent.com/68587990/201023743-e1b368d6-c0f6-42f9-a431-7663623d292b.png)



전 과는 다르게 Runnable과 Serializable 인터페이스를 모두 구현하고 있는것을 볼 수 있다!

>
> 💡 `(Runnable & Serializable)` 방식으로 타입 캐스팅을 해주어도 되지만 이는 보일러 플레이트 코드가 될 수 있으므로
> 명시적으로 두 인터페이스를  상속한 커스텀한 인터페이스를 만든 뒤 타입캐스트를 하면 더 깔끔하다
> ex)
> interface SerializableRunnable extends Runnable, Serializable { }
>
> (SerializableRunnable) () → System.out.println(””);

타입 캐스트를 해준뒤, 다시 한번 main 메소드로 테스트를 해보자!

![%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-10_15 51 17](https://user-images.githubusercontent.com/68587990/201024007-43ad1e5f-dae1-4ab8-9b93-de67cf46c7d0.png)


정상적으로 잘 동작하는것을 볼 수 있다..👏🏻👏🏻

람다 직렬화의 기본 메커니즘에 대해 더 상세히 알고 싶으면 [Serialize a Lambda in Java](https://www.baeldung.com/java-serialize-lambda)를 참고하자

---

## 정리

- 람다는 일반적인 직렬화 방식으로는 직렬화 되지 않으므로 타입 캐스트를 해야한다
- 하지만 공식적으로는 람다나 익명 클래스에 대한 직렬화를 ‘강하게’ 권장하지 않는다
- 꼭 필요한 경우가 아니라면 리팩터링 하는것을 권장한다