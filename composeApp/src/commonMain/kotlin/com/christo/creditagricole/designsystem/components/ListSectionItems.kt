import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.christo.creditagricole.designsystem.components.Divider
import com.christo.creditagricole.designsystem.components.SectionTitle

@Composable
fun <T> ListSectionItems(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    showDividers: Boolean = true,
    itemContent: @Composable (T) -> Unit
) {
    Column(modifier) {
        SectionTitle(title)
        Spacer(Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            itemContent(item)
            if (showDividers && index != items.lastIndex) {
                Divider(Modifier.fillMaxWidth())
            }
        }
    }
}