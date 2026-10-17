package app.cashjournal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import app.cashjournal.data.backup.TransactionBackupDto

@Entity(
    tableName = "change_history",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class ChangeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val actionType: String,
    val entityType: String,
    val entityId: String,
    val summary: String,
    val previousStateJson: String? = null,
    val newStateJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SplitGroupPayload(
    val transactions: List<TransactionBackupDto>
)
