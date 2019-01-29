package e.darom.todolist

import android.app.Application
import io.realm.Realm

/*
애플리케이션 객체에서 Realm 초기화
1. Application 클래스를 상속받는 MyApplication  클래스를 선언합니다.

2. onCreate() 메서드를 오버라이드합니다. 이 메서드는 액티비티가 생성되기 전에 호출됩니다.

3. Realm.init() 메서드를 사용하여 초기화합니다.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

}