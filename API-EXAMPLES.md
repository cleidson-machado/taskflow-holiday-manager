# Exemplos de Uso da API de Bookings (Atualizada)

## 📌 Endpoint Base
```
http://localhost:8080/bookings
```

---

## 1️⃣ Criar um Novo Booking (POST)

### Request
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "startDate": "2025-11-13",
    "daysReserved": 5,
    "requestNotes": "Férias de fim de ano"
  }'
```

### Response (200 Created)
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "employee": {
    "id": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "name": "João",
    "surname": "Silva",
    "fiscalNumber": "12345678900"
  },
  "vacationId": null,
  "startDate": "2025-11-13",
  "endDate": "2025-11-19",
  "daysReserved": 5,
  "bookingStatus": "RESERVED",
  "isActive": true,
  "requestNotes": "Férias de fim de ano",
  "createdAt": "2025-11-12T10:30:00",
  "updatedAt": "2025-11-12T10:30:00",
  "deletedAt": null,
  "deletedBy": null
}
```

### ✅ Observações:
- O `endDate` foi calculado automaticamente como **19/11/2025** (quarta-feira)
- O sistema pulou o fim de semana (15 e 16/11)
- 5 dias úteis: 13, 14, 17, 18, 19

---

## 2️⃣ Atualizar um Booking (PUT)

### Request - Aumentar para 10 dias
```bash
curl -X PUT http://localhost:8080/bookings/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "startDate": "2025-11-13",
    "daysReserved": 10,
    "requestNotes": "Férias estendidas"
  }'
```

### Response (200 OK)
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "employee": {
    "id": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "name": "João",
    "surname": "Silva",
    "fiscalNumber": "12345678900"
  },
  "vacationId": null,
  "startDate": "2025-11-13",
  "endDate": "2025-11-26",
  "daysReserved": 10,
  "bookingStatus": "RESERVED",
  "isActive": true,
  "requestNotes": "Férias estendidas",
  "createdAt": "2025-11-12T10:30:00",
  "updatedAt": "2025-11-12T11:15:00",
  "deletedAt": null,
  "deletedBy": null
}
```

### ✅ Observações:
- O `endDate` foi recalculado para **26/11/2025**
- Pulou 2 fins de semana (15-16/11 e 22-23/11)

---

## 3️⃣ Listar Todos os Bookings (GET)

### Request
```bash
curl -X GET "http://localhost:8080/bookings?page=0&size=10&sortField=startDate&sortOrder=asc"
```

### Response (200 OK)
```json
{
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "employee": {...},
      "startDate": "2025-11-13",
      "endDate": "2025-11-26",
      "daysReserved": 10,
      "bookingStatus": "RESERVED",
      ...
    }
  ],
  "totalItems": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

---

## 4️⃣ Buscar Booking por ID (GET)

### Request
```bash
curl -X GET http://localhost:8080/bookings/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Response (200 OK)
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "employee": {...},
  "startDate": "2025-11-13",
  "endDate": "2025-11-26",
  "daysReserved": 10,
  ...
}
```

---

## 5️⃣ Buscar Bookings Ativos de um Colaborador (GET)

### Request
```bash
curl -X GET http://localhost:8080/bookings/employee/09b6c347-8362-48d4-b715-81350fff7ce7
```

### Response (200 OK)
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "startDate": "2025-11-13",
    "endDate": "2025-11-26",
    "daysReserved": 10,
    ...
  }
]
```

---

## 6️⃣ Cancelar um Booking (PUT)

### Request
```bash
curl -X PUT "http://localhost:8080/bookings/a1b2c3d4-e5f6-7890-abcd-ef1234567890/cancel?cancelledBy=admin"
```

### Response (200 OK)
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "bookingStatus": "CANCELLED",
  "isActive": true,
  ...
}
```

---

## 7️⃣ Vincular a uma Vacation (PUT)

### Request
```bash
curl -X PUT http://localhost:8080/bookings/a1b2c3d4-e5f6-7890-abcd-ef1234567890/link-vacation/vacation-uuid-here
```

### Response (200 OK)
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "vacationId": "vacation-uuid-here",
  ...
}
```

---

## 8️⃣ Soft Delete (DELETE)

### Request
```bash
curl -X DELETE "http://localhost:8080/bookings/a1b2c3d4-e5f6-7890-abcd-ef1234567890?deletedBy=admin"
```

### Response (200 OK)
```json
{
  "message": "Booking soft deleted successfully"
}
```

---

## ⚠️ Exemplos de Erros

### ❌ Erro: Data no passado
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "startDate": "2025-01-01",
    "daysReserved": 5
  }'
```

**Response (400 Bad Request):**
```json
{
  "error": "Cannot create booking for past dates."
}
```

---

### ❌ Erro: daysReserved inválido
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "startDate": "2025-12-01",
    "daysReserved": 0
  }'
```

**Response (400 Bad Request):**
```json
{
  "error": "Days reserved must be greater than zero."
}
```

---

### ❌ Erro: Conflito de datas
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
    "startDate": "2025-11-15",
    "daysReserved": 5
  }'
```

**Response (409 Conflict):**
```json
{
  "error": "There is already an active booking for this period."
}
```

---

## 🧪 Teste Completo - Cenário Real

### Cenário: João quer 5 dias de férias a partir de 13/11/2025

1. **Criar Booking**
```bash
POST /bookings
{
  "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
  "startDate": "2025-11-13",
  "daysReserved": 5,
  "requestNotes": "Viagem em família"
}
```

2. **Sistema Calcula**:
   - 13/11 (quinta) ✅
   - 14/11 (sexta) ✅
   - 15/11 (sábado) ❌ (fim de semana)
   - 16/11 (domingo) ❌ (fim de semana)
   - 17/11 (segunda) ✅
   - 18/11 (terça) ✅
   - 19/11 (quarta) ✅
   
3. **Resultado**: `endDate = "2025-11-19"`

4. **João decide estender para 10 dias**
```bash
PUT /bookings/{id}
{
  "employeeId": "09b6c347-8362-48d4-b715-81350fff7ce7",
  "startDate": "2025-11-13",
  "daysReserved": 10,
  "requestNotes": "Viagem em família - estendida"
}
```

5. **Sistema Recalcula**: `endDate = "2025-11-26"`

---

## 📅 Feriados Considerados (2025)

| Data | Feriado |
|------|---------|
| 01/01 | Confraternização Universal |
| 21/04 | Tiradentes |
| 01/05 | Dia do Trabalho |
| 07/09 | Independência do Brasil |
| 12/10 | Nossa Senhora Aparecida |
| 02/11 | Finados |
| 15/11 | Proclamação da República |
| 25/12 | Natal |
| + | Carnaval, Sexta-feira Santa, Corpus Christi (móveis) |

---

## 🎯 Comparação: Antes vs Depois

### ❌ Antes (cliente calculava)
```json
{
  "startDate": "2025-11-13",
  "endDate": "2025-11-17"  // ❌ Errado! Não considerou fim de semana
}
```

### ✅ Agora (sistema calcula)
```json
{
  "startDate": "2025-11-13",
  "daysReserved": 5
}
// Sistema retorna: "endDate": "2025-11-19" ✅ Correto!
```

