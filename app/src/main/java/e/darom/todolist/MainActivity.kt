package e.darom.todolist

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.startActivity

/*
많은 양이 반복되는 아이템을 표시할 때는 리스트 뷰를 사용합니다.
스크롤 뷰 :
- 적은 양의 아이템을 스크롤 시킬 떄 간단히 사용합니다.
- 한 번에 모든 아이템을 메모리에 로드하여 상황에 따라 많은 메모리가 요구됩니다.

리스트 뷰 :
- 많은 양의 반복되는 아이템을 표시할 때 사용합니다.
- 뷰를 재사용하므로 적은 메모리를 사용하고 화면에 보이는 것만 동적으로 로딩합니다.

리스트 뷰를 사용하려면 데이터와 데이터를 표현하는 어댑터를 작성해야 합니다.
어댑터란 데이터를 리스트 뷰에 어떻게 표시할지 정의하는 객체입니다. 어댑터를 작성하기에 따라서 리스트 뷰의 성능에도 큰 영향을 미치기 때문에 어댑터 작성은 아주 중요합니다.


 */
class MainActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance() // 1. 객체 초기화

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // 새 할 일 추가
        /*
        FAB 를 클릭했을 때 EditActivity 액티비티를 시작하도록 수정합니다.

        앱을 실행하여 새 할 일을 추가합니다. 추가되었다는 다이얼로그가 표시되고 첫 화면으로 돌아오면 성공입니다.
        아직 첫 화면의 리스트 뷰를 작성하지 않았기 때문에 목록이 표시되지는 않습니다.
         */
        fab.setOnClickListener{
            startActivity<EditActivity>()
        }

        // 2. 전체 할 일 정보를 가져와서 날짜순으로 내림차순 정렬
        /*
         1. TodoListAdapter 클래스에 할 일 목록인 realmResult 를 연결하여 어댑터 인스턴스를 생성합니다.
         2. 생성한 어댑터를 리스트 뷰에 설정합니다. 이것으로 할 일 목록이 리스트 뷰에 표시됩니다.

         Realm 의 또 다른 장점은 데이터가 변경되는지 모니터링할 수 있다는 점입니다.
         3. addChangeListener 를 구현하면 데이터가 변경될 때마다 어댑터에 알려줄 수 있습니다.
         어댑터에 notifyDataSetChange() 메서드를 호출하면 데이터 변경을 통지하여 리스트를 다시 표시하게 됩니다.

         리스트 뷰의 아이템을 클릭했을 때의 처리를 setOnItemClickListener 메서드에 구현합니다.
         4. EditActivity 에 선택한 아이템의 id 값을 전달합니다. 이제 기존 id 가 있는지 여부에 따라 새 할일을 추가하거나 기존 할 일을 수정할 수 있습니다.

         앱을 실행하여 추가한 할 일 목록이 첫 화면의 리스트에 표시되는지 확인합니다. 표시된 목록을 클릭하여 수정과 삭제가 잘 동작하면 성공입니다.
         */
        val realmResult = realm.where<Todo>().findAll().sort("date", Sort.DESCENDING)
        val adapter = TodoListAdapter(realmResult)  // 1.
        listView.adapter = adapter  // 2.

        // 3. 데이터가 변경되면 어댑터에 적용
        realmResult.addChangeListener { _ -> adapter.notifyDataSetChanged() }

        listView.setOnItemClickListener{parent, view, posisition, id ->
            // 4. 할 일 수정
            startActivity<EditActivity>("id" to id)
        }

        // 새 할 일 추가
        fab.setOnClickListener{
            startActivity<EditActivity>()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close() // 3. 객체 해제
    }

}
