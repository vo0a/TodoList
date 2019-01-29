package e.darom.todolist

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_edit.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.util.*

/*
MyApplication 클래스에서 Realm 을 초기화했다면
액티비티에서는 1. Realm.getDefaultInstance() 메서드를 이용해 Realm 객체의 인스턴스를 얻을 수 있습니다.

2. 사용이 끝난 인스턴스는 액티비티가 소멸되는 생명주기인 onDestroy() 에서 해제합니다.

두 번째 화면에서는 할 일을 추가하거나 수정합니다. 그래서 추가 모드와 수정 모드를 분리하는 규칙을 정해야 합니다.
 id 를 -1 로 초기화하고 수정 시에는 id 를 인텐트에 포함하여 받아옵니다. id 가 -1 이면 추가 모드이고, 아니면 수정 모드가 되도록 합니다.
 추가 모드와 수정 모드를 분기하여 처리하는 코드를 다음과 같이 추가합니다.

 getLongExtra(name: String, defaultValue: Long) // 인텐트로부터 데이터를 꺼내 반환합니다.
 - name : 아이템이 가리키는 키(Key) 입니다.
 - defaultValue : 반환되는 값이 없을 때 기본값을 설정합니다.

 1. id 값이 0 이상이면 업데이트 모드이고, 무엇도 넘어오지 않아 기본값 그대로 -1 이라면 추가 모드 입니다.

 2. CalendarView 에서 날짜를 선택하면 수행할 처리를 setOnDateCahngeListener() 메서드로 구현합니다. 변경된 년, 월, 일이 year, month, dayOfMonth 로 넘어오므로
 Calendar 객체에서 년, 월, 일을 설정해주면 데이터베이스에 추가, 수정 시 설정한 날짜가 반영됩니다.

 3. 추가 모드일 때는 4. 삭제 버튼을 숨깁니다. 뷰를 보이거나 안 보이게 하려면 setVisivility() 메서드를 사용합니다.
 코틀린에서는 visivility 프로퍼티를 사용할 수 있습니다. visivility 프로퍼티에는 다음과 같은 속성을 지정할 수 있습니다.
 - VISIVLE  :  보이게 합니다.
 - INVISIVLE : 영역은 차지하지만 보이지 않습니다.
 - GONE : 완전히 보이지 않습니다.

 5. 추가 모드에서는 완료 버튼을 누르면 할 일을 추가합니다.

 6. 수정 모드에서는 id 를 전달받아야 합니다. 7. 전달받은 id 를 가진 할 일 데이터를 찾아서 화면에 데이터를 표시합니다.
 수정 모드에서는 8. 완료 버튼을 누르면 해당 아이템이 수정되고 9. 삭제 버튼을 누르면 삭제됩니다.
 */
class EditActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance() // 1. 인스턴스 얻기

    val calendar = Calendar.getInstance()     // 날짜를 다룰 캘린더 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // 1. 업데이트 조건
        val id = intent.getLongExtra("id", -1L)
        if(id == -1L){
            insertMode()
        } else{
            updateMode(id)
        }

        // 2. 캘린더 뷰의 날짜를 선택했을 때 Calendar 객체에 설정
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
    }

    // 3. 추가 모드 초기화
    @SuppressLint("RestrictedApi")
    private fun insertMode(){
        // 4. 삭제 버튼을 감추기
        deleteFab.visibility = View.GONE

        // 5. 완료 버튼을 클릭하면 추가
        doneFab.setOnClickListener{
            insertTodo()
        }
    }

    // 6. 수정 모드 초기화
    private fun updateMode(id: Long){
        // 7. id 에 해당하는 객체를 화면에 표시
        val todo = realm.where<Todo>().equalTo("id", id).findFirst()!!
        todoEditText.setText(todo.title)
        calendarView.date = todo.date

        // 8. 완료 버튼을 클릭하면 수정
        doneFab.setOnClickListener{
            updateTodo(id)
        }

        // 9. 삭제 버튼을 클릭하면 삭제
        deleteFab.setOnClickListener{
            deleteTodo(id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()                              // 2. 인스턴스 해제
    }

    // 할 일 입력 메서드 작성
    /*
    날짜를 다루는 Calendar 클래스 사용 방법

    1.  Calendar 객체를 생성할 때는 getInstance() 메서드로 생성합니다. 객체를 생성한 오늘 날짜로 초기화됩니다.

    2. set() 메서드에 변경할 필드와 값을 지정하여 년, 월, 일 등을 지정하여 변경할 수 있습니다. 지정하는 필드는 Calendar 클래스에 상수로 정의되어 있습니다.
    set(field: Int, value: Int) // 주어진 필드에 값을 변경합니다.
    - field : 변경할 필드 (YEAR, MONth, DAY_OF_MONTH)
    - value : 변경할 값

    3. 데이터베이스에는 시간이나 날짜를 Long 형으로 저장할 겁니다. Calendar 객체는 Long 형값으로 변환하는 getTimeInMilles() 메서드를 제공합니다.
    코틀린에서는 timeInMillis 프로퍼티로 사용할 수 있습니다.

    Realm 에서 데이터를 추가, 삭제, 업데이트할 때는 1. beginTransaction() 메서드로 트랜잭션을 시작합니다.
    트랜잭션이란 데이터베이스의 작업 단위입니다. 1. beginTransaction() 메서드와 4. commitTransaction() 메서드 사이에 작성한 코드들은 전체가 하나의 작업으로
    도중에 에러가 나면 모두 취소됩니다. 이 과정이 모두 한 트랜잭션이기 때문입니다. 따라서 트랜잭션은 데이터베이스에서 아주 중요한 개념입니다.
    데이터베이스에 추가, 삭제, 업데이트를 하려면 항상 트랜잭션을 시작하고 닫아야 합니다.

    2. createObject() 메서드로 새로운 Realm 객체를 생성합니다.
    createObject<T: RealmModel>(primaryKeyValue: Any?) // 새로운 T 타입의 Realm 객체를 생성합니다.
    - primaryKeyValue : 기본키를 지정합니다.

    Realm 은 기본키 자동 증가 기능을 지원하지 않습니다. 그래서 6. 과 같이 현재 가장 큰 id 값을 얻고 1 을 더한 값을 반환하는 메서드를 추가로 작성했습니다.
    객체 생성 시 id 값을 계산할 때 사용합니다.

    Todo 테이블의 모든 값을 얻으려면 wehre<Todo>() 메서드를 사용합니다. 이 메서드는 RealmQuery 객체를 반환하고 다음에 이어지는 조건을 수행합니다.
    여기서는 max() 메서드를 조건으로 달았습니다.  max(0 메서드는 현재 id 중 가장 큰 값을 얻을 때 사용합니다.
    max(fieldName: String) //fieldName 열 값 중 가장 큰 값을 Number 형으로 반환합니다.
    - fieldName : 찾고자 하는 열 이름

    3. 객체를 생성했다면 할 일과 시간을 설정합니다.
    할 일이 추가되면 5. 다이얼로그를 표시합니다. 다이얼 로그 확인 버튼을 누르면 finish() 메서드를 호출하여 현재 액티비티를 종료합니다.
     */
    private fun insertTodo(){
        realm.beginTransaction()    // 1. 트랜잭션 시작

        val newItem = realm.createObject<Todo>(nextId())    // 2. 새 객체 생성
        // 3. 값 설정
        newItem.title = todoEditText.text.toString()
        newItem.date = calendar.timeInMillis

        realm.commitTransaction()   // 4. 트랜잭션 종료 반영

        // 5. 다이얼 로그 표시
        alert("내용이 추가되었습니다."){
            yesButton { finish() }
        }.show()
    }

    // 6. 다음 id를 반환
    private fun nextId(): Int{
        val maxId = realm.where<Todo>().max("id")
        if(maxId != null){
            return maxId.toInt() +1
        }
        return 0
    }

    /*
    할 일 업데이트 메서드 작성
    1. updateTodo() 메서드는 id를 인자로 받습니다.

    2. Realm 객체의 where<T>() 메서드가 반환하는 T 타입 객체로부터 데이터를 얻습니다. equalTo() 메서드로 조건을 설정합니다.
    "id" 컬럼에 id 값이 있다면 findFirst() 메서드로 첫 번째 데이터를 반환합니다.(여기서 !! 는 무슨 뜻이지? → !!는 타입의 값이 null 값이 아님을 보증합니다.)
    (+추가 ?. 연산자는 안전한 호출로 null값이 아닌 경우에만 호출됩니다.)

    나머지 코드는 할 일 추가와 동일합니다.
     */
    private fun updateTodo(id: Long){ // 1.
        realm.beginTransaction()    // 트랜잭션 시작

        val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!!    // 2.
        // 3. 값 수정
        updateItem.title = todoEditText.text.toString()
        updateItem.date = calendar.timeInMillis

        realm.commitTransaction()   // 4. 트랜잭션 종료 반영

        // 5. 다이얼 로그 표시
        alert("내용이 변경되었습니다."){
            yesButton { finish() }
        }.show()
    }

    // 할 일 삭제 메서드 작성 : 메서드로 전달받은 id 로 삭제할 객체를 검색하고 팢았다면 deleteFromRealm() 메서드로 삭제합니다.
    private fun deleteTodo(id: Long){
        realm.beginTransaction()
        val deleteItem = realm.where<Todo>().equalTo("id", id).findFirst()!!
        // 삭제할 객체
        deleteItem.deleteFromRealm()    // 삭제
        realm.commitTransaction()
        alert("내용이 삭제되었습니다."){
            yesButton { finish() }
        }.show()
    }
}
