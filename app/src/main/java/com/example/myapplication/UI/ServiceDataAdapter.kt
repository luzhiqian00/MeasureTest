import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.R
    import com.example.myapplication.ble.ServiceData

    class ServiceDataAdapter : BaseAdapter(){
        private val serviceList = ArrayList<ServiceData>()

        fun setServiceList(serviceData:ArrayList<ServiceData>){
            serviceList.addAll(serviceData)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return serviceList.size
        }

        override fun getItem(position: Int): Any {
            return serviceList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val service = serviceList[position]
            val view = LayoutInflater.from(parent?.context).inflate(
                R.layout.bluetooth_services_item_layout, parent, false
            )

            // Find the TextViews in your layout
            var serviceNameTextView = view.findViewById<TextView>(R.id.serviceNameTextView)
            var serviceUUIDTextView = view.findViewById<TextView>(R.id.serviceUUIDTextView)
            var characteristics = view.findViewById<TextView>(R.id.characteristics)
            // Set the text of the TextViews to the service name and UUID
            serviceNameTextView.text = service.serviceName ?: "Unknown Device"
            serviceUUIDTextView.text = service.serviceUUID.toString() ?: "Unknown UUID"

            var characteristicsString = ""
            service.characteristics.let { characteristics ->
                characteristics.forEach { characteristic ->
                    // 在这里执行您的操作，例如拼接到字符串
                    characteristicsString += "${characteristic.characteristicName} " +
                            "${characteristic.characteristicUUID.toString()}\n"+
                            "${characteristic.characteristicVal}\n"
                }
            }
            characteristics.text = characteristicsString

            return view
        }

    }