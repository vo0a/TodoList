package e.darom.todolist

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/*
모델 클래스 앞에 open 을 붙이고 RealmObject 클래스를 상속받으면
Realm 에서 테이블로 사용이 가능합니다.

1. 코틀린에서는 Realm 에서 사용하는 클래스에 open 키워드를 추가합니다.

2. id 는 유일한 값이 되어야 하기 때문에 기본티제약을 주석으로 추가합니다. 이 주석이 부여된 속성값은 중복을 허용하지 않습니다.

3. RealmObject 클래스를 상속받아 Realm 데이터베이스에서 다룰 수 있습니다.
 */
open class Todo(@PrimaryKey var id: Long = 0, var title: String ="", var date: Long =0) : RealmObject() {

}