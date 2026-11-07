package com.example.recordatoriosappavilaroque.data

// De Entidad (Base de datos) a Modelo (Interfaz)
fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        title = title,
        description = description,
        category = try { Category.valueOf(category) } catch (e: Exception) { Category.HOGAR },
        priority = try { Priority.valueOf(priority) } catch (e: Exception) { Priority.MEDIA },
        status = try { ReminderStatus.valueOf(status) } catch (e: Exception) { ReminderStatus.PENDIENTE },
        date = date,
        syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (e: Exception) { SyncStatus.SYNCED }
    )
}

// De Modelo a Entidad
fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        title = title,
        description = description,
        category = category.name,
        priority = priority.name,
        status = status.name,
        date = date,
        syncStatus = syncStatus.name
    )
}
// De DTO (Remoto) a Entidad (Room)
fun ReminderDto.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = this.id ?: 0,
        title = this.title,
        description = this.description,
        category = Category.HOGAR.name, // El servidor no manda esto, pongo uno por defecto
        priority = Priority.MEDIA.name,
        status = ReminderStatus.PENDIENTE.name,
        date = System.currentTimeMillis(),
        syncStatus = SyncStatus.SYNCED.name // Viene del servidor, así que está sincronizado
    )
}

// De Entidad (Room) a DTO (Remoto)
fun ReminderEntity.toDto(): ReminderDto {
    return ReminderDto(
        id = if (this.id == 0) null else this.id,
        title = this.title,
        description = this.description
    )
}