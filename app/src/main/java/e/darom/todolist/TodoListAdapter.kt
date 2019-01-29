package e.darom.todolist

import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter
import org.w3c.dom.Text

/*
일반적으로 리스트 뷰용 어댑터는 BaseAdapter 클래스를 상속받아서 작성하지만 Realm 을 사용할 때는 Realm 에서 제공하는 RealmBaseAdapter 클래스를 상속받습니다.
RealmBaseAdapter 를 사용하려면 우선 모듈 수준의 build.gradle 파일에 라이브러리 의존성을 추가하고 싱크합니다.
- implementation 'io.realm:android-adapters:2.1.1'

RealmBaseAdapter 를 상속받습니다.
RealmBaseAdapter 는 OrderedRealmCollection<T> 형 데이터를 받는 주 생서자를 가지고 있습니다.
RealmBaseAdapter 클래스는 미구현 메서드가 포함된 추상 클래스이기 때문에 상속받은 클래스는 이를 구현해야 합니다.
Implement members 를 클릭하여 미구현 메서드를 구현합니다. getView() 메서드가 오버라이드 됩니다.

getView() 메서드에서 리스트 뷰의 각 아이템에 표시할 뷰를 구성합니다. 각 아이템은 화면에 보이기 전에 getView() 메서드가 한 번씩 호출됩니다.
getView(position: Int, convertView: View?, parent: ViewGroup?): View
- position : 리스트 뷰의 아이템 위치입니다.
- convertView : 재활용되는 아이템 뷰입니다.
- parent : 부모 뷰 즉 여기서는 리스트 뷰의 참조를 가리킵니다.

* 뷰 홀더 패턴 적용
리스트 뷰에서는 리스트를 표시할 때 성능을 향상시킬 목적으로 일반적으로 뷰 홀더 패턴을 적용합니다. getView() 메서드가 아이템이 화면에 표시될 때마다 호출되므로
최대한 효율적인 코드를 작성해야 하기 때문입니다. 뷰 홀더 패턴은 한 번 작성한 레이아웃을 재사용하고 내용만 바꾸는 방법입니다.
리스트 뷰에 뷰 홀더 패턴을 적용하면 스크롤 시 5, 6번 아이템은 0, 1번 아이템의 뷰를 재사용해 내용만 바꾸기 때문에 매번 뷰를 새로 생성할 필요가 없습니다.
따라서 뷰 홀더 패턴을 한 번 만들어둔 뷰를 최대한 재활용하여 성능을 높여주는 방법이라고 알아두면 됩니다.

getView() 메서드를 작성하기 전에 아이템에 표시할 레이아웃 리소스 파일을 먼저 만들어 둡시다.
res/layout 폴더에서 마우스 우클릭하여 File -> New -> Fayout resource file 을 클릭합니다.
파일 이름을 item_todo 로 입력하고 OK 를 클릭합니다.

 */

/*
getView() 메서드는 매 아이템이 화면에 보일 떄마다 호출됩니다. getView() 메서드의 두 번째 인자인 convertView 는 아이템이 작성되기 전에는 null 이고 한 번 작성되면 이전에 작성했던 뷰를 전달합니다.
1. convertVew 가 null 이면 레이아웃을 작성합니다.

2. LayoutInflater 클래스는 XML 레이아웃 파일을 코드로 불러오는 기능을 제공합니다.
 LayoutInflater.from (parent?.context) 메서드로 객체를 얻고 inflate() 메서드로 XML 레이아웃 파일을 읽어서 뷰로 반환하여 view 변수에 할당합니다.
 inflate(resource: Int, root: ViewGroup, attachToRoot: Boolean)
 - resource : 불러올 레이아웃 XML 리소스 ID를 지정합니다.
 - root : 불러온 레이아웃 파일이 붙을 뷰그룹인 parent 를 지정합니다.
 - attachToRoot : XML 파일을 불러올 때는 false 를 지정합니다.

 3. 뷰 홀더 객체를 초기화합니다. 뷰 홀더 클래스는 15. 와 같이 별도의 클래스로 먼저 작성합니다.
 뷰 홀더 클래스는 전달받은 view 에서 text1 과 text2 아이디를 가진 텍스트 뷰들의 참조를 저장하는 역할을 합니다.

 4. 뷰 홀더 객체는 tag 프로퍼티로 view 에 저장됩니다. tag 프로퍼티에는 Any 형으로 어떠한 객체도 저장할 수 있습니다.

 5. convertView 가 null 이 아니라면 6. 이전에 작성했던 convertView 를 재사용합니다. 그리고 7. 뷰 홀더 객체를 tag 프로퍼티에서 꺼냅니다.
 반환되는 데이터형이 Any 이므로 ViewHolder 형으로 형변환을 합니다.

 RealmBaseAdapter 는 adapterData 프로퍼티를 제공합니다. 여기서 데이터에 접근할 수 있습니다.
 8. 값이 있다면 9. 해당 위치의 데이터를 item 변수에 담습니다. 10. 할 일 텍스트와 11. 날짜를 각각 텍스트 뷰에 표시합니다.
 DateFormat.format() 메서드는 지정한 형식으로 Long 형 시간 데이터를 변환합니다. DateFormat 클래스는 android.text.format.DateFormat 을 임포트하는 것에 주의합니다.

 12. 완성된 view 변수를 반환합니다. 이 뷰는 다음 번에 호출되면 convertView 로 재사용됩니다.

 13. getItemId() 메서드를 오버라이드합니다. 리스트 뷰를 클릭하여 이벤트를 처리할 때 인자로 position, id 등이 넘어오게 되는데 이때 넘어오는 id 값을 결정합니다.
 데이터베이스를 다룰 때 레코드마다 고유한 아이디를 가지고 있는데 그것을 반환하도록 정의합니다.

 adapterView 가 Realm 데이터를 가지고 있으므로 14. 요청한 해당 위치에 있는 데이터의 id 값을 반환하도록 합니다.
 */
class TodoListAdapter(realmResult: OrderedRealmCollection<Todo>) : RealmBaseAdapter<Todo>(realmResult) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh: ViewHolder
        val view: View

        if(convertView == null){    // 1.
            view = LayoutInflater.from(parent?.context).inflate(R.layout.item_todo, parent, false) // 2.

            vh = ViewHolder(view)  // 3.
            view.tag = vh   // 4.
        } else{     // 5.
            view = convertView  // 6.
            vh = view.tag as ViewHolder    // 7.
        }

        if(adapterData != null){ // 8.
            val item = adapterData!![position]  // 9.
            vh.textTextView.text = item.title
            vh.dateTextView.text = DateFormat.format("yyyy/MM/dd", item.date)   // 11.
        }

        return view // 12.
    }

    override fun getItemId(position: Int): Long{ // 13.
        if(adapterData != null){
            return adapterData!![position].id   // 14.
        }
        return super.getItemId(position)
    }

    // 15.
    class ViewHolder(view: View){
        val dateTextView: TextView = view.findViewById(R.id.text1)
        val textTextView: TextView = view.findViewById(R.id.text2)
    }
}