# Bank Microservices

[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Описание

Этот проект представляет собой учебную реализацию банковской системы на основе микросервисной архитектуры. Разработан в рамках курса на Udemy для демонстрации ключевых принципов построения распределенных систем.

Проект демонстрирует:
- Разделение бизнес-логики на независимые сервисы.
- Взаимодействие между сервисами через синхронные (REST) и асинхронные (события) каналы.
- Централизованное конфигурирование, сервис-дискавери и маршрутизацию.
- Внедрение распределенной трассировки для мониторинга.

## Архитектура и технологии

Проект состоит из следующих ключевых компонентов:

| Микросервис | Описание | Порт (внутренний) |
| :--- | :--- | :--- |
| **Config Server** | Центральный сервер конфигурации для всех сервисов. | `8071` |
| **Eureka Server** | Сервер служб (Service Discovery) для регистрации и поиска микросервисов. | `8070` |
| **Gateway Server** | API-шлюз, единая точка входа для всех клиентских запросов. | `8072` |
| **Accounts** | Сервис для управления банковскими счетами. | `8080` |
| **Loans** | Сервис для управления кредитами. | `8090` |
| **Cards** | Сервис для управления банковскими картами. | `9000` |
| **Message** | Сервис для обработки асинхронных событий (например, коммуникация между Accounts и Loans). | `9010` |

**Важно:** Все внешние запросы направляются **только через API-шлюз** на порт `8072`. Прямой доступ к микросервисам извне закрыт.

**Основные технологии:**
- **Язык:** Java 17+
- **Фреймворк:** Spring Boot 3.x, Spring Cloud
- **Безопасность:** Keycloak (OAuth2 / OpenID Connect)
- **Взаимодействие:** REST API, асинхронные сообщения (через `message` сервис)
- **Мониторинг:** OpenTelemetry для распределенной трассировки
- **Контейнеризация:** Docker и Docker Compose

## Установка и запуск

### Предварительные требования
- Установленные **Docker** и **Docker Compose**.
- Свободные порты, указанные в таблице выше.

### Инструкция по запуску

1.  **Клонируйте репозиторий:**
    ```bash
    git clone https://github.com/TheGreenUp/bank_microservices.git
    cd bank_microservices
    ```

2.  **Запустите все сервисы с помощью Docker Compose:**
    Для запуска используется предварительно сконфигурированный `docker-compose` файл для продакшн-окружения.
    ```bash
    docker-compose -f docker-compose/prod/docker-compose.yml up -d
    ```
    > **Примечание:** Первый запуск может занять несколько минут, так как Docker будет скачивать необходимые образы и собирать приложения.

3.  **Проверьте работу сервисов:**
    - Eureka Dashboard: [http://localhost:8070](http://localhost:8070)
    - Gateway Server доступен на: [http://localhost:8072](http://localhost:8072)

## Настройка безопасности (Keycloak)

Для доступа к защищенным эндпоинтам микросервисов необходимо настроить Keycloak. Проект использует Keycloak, запущенный в Docker-контейнере.

1.  **Доступ к консоли администрирования Keycloak:**
    - Откройте в браузере: `http://localhost:7080`.
    - Используйте учетные данные, заданные в переменных окружения `docker-compose.yml` (`admin` / `admin`).

2.  **Настройка Realm и клиентов:**
    - Создайте новый Realm (например, `master`) или используйте существующий.
    - Создайте клиентов (Clients) для авторизации:
      - `eazybank-callcenter-cc` (для Client Credentials)
      - `eazybank-callcenter-ac` (для Authorization Code)
    - Настройте для них соответствующие `client_secret`.

3.  **Создание ролей и пользователей:**
    - В вашем Realm создайте роли: `ACCOUNTS`, `LOANS`, `CARD`.
    - Создайте тестовых пользователей (например, `user` / `password`) и назначьте им соответствующие роли. Это необходимо для авторизации доступа к конкретным сервисам.

4.  **Настройка сервисов:**
    - Убедитесь, что в файлах конфигурации (или в Config Server) для каждого микросервиса правильно указаны `issuer-uri` и другие параметры подключения к вашему Keycloak.

После выполнения этих шагов приложение будет готово к работе, и доступ к эндпоинтам будет осуществляться на основе выданных JWT-токенов.

## API Эндпоинты (Postman)

В репозитории находится файл **[Endpoints for Postman.json](Endpoints%20for%20Postman.json)** с полной коллекцией запросов для тестирования всех микросервисов через API-шлюз.

### Импорт коллекции
1. Откройте Postman.
2. Нажмите **Import** → **Upload Files**.
3. Выберите файл `Endpoints for Postman.json` из корня проекта.
4. Коллекция `Microservices` будет загружена со всеми предварительно настроенными запросами.

### Доступные группы запросов

| Группа | Описание | Примеры эндпоинтов (через шлюз) |
| :--- | :--- | :--- |
| **gatewayserver** | Запросы к микросервисам через API-шлюз | `POST /eazybank/accounts/api/create`, `GET /eazybank/accounts/api/fetch` |
| **gatewayserver_security** | Тестирование безопасности с разными типами аутентификации | `GET` (PermitAll), `POST` (ClientCredentials), `POST` (AuthCode) |
| **KeyCloak** | Получение JWT-токенов | `POST /realms/master/protocol/openid-connect/token` |
| **message** | Отправка асинхронных сообщений (тестовый эндпоинт) | `POST /email`, `POST /sms`, `POST /emailsms` |
| **configserver** | Шифрование/дешифрование конфигурации | `POST /encrypt`, `POST /decrypt` |
| **eurekaserver** | Информация о зарегистрированных сервисах | `GET /eureka/apps/{service-name}` |

> **Примечание:** Эндпоинты для `accounts`, `cards`, `loans` доступны только через `gatewayserver_security` и защищены OAuth2.

### Настройка авторизации в Postman

Для работы с защищенными эндпоинтами (`POST` запросы) необходимо настроить авторизацию в коллекции:

**Вариант 1: Client Credentials (для сервис-ту-сервис)**
1. Откройте запрос из группы `gatewayserver_security` → `Accounts_POST_ClientCredentials`.
2. Перейдите на вкладку **Authorization**.
3. Выберите тип **OAuth 2.0**.
4. Заполните параметры:
   - `Token Name`: `clientcredentails_accesstoken`
   - `Grant Type`: `Client Credentials`
   - `Access Token URL`: `http://localhost:7080/realms/master/protocol/openid-connect/token`
   - `Client ID`: `eazybank-callcenter-cc`
   - `Client Secret`: `возьметё из keycloak-credentials`
   - `Scope`: `openid email profile`
   - `Client Authentication`: `Send client credentials in body`
5. Нажмите **Get New Access Token**, затем **Use Token**.

**Вариант 2: Authorization Code (для пользователей)**
1. Откройте запрос из группы `gatewayserver_security` → `Accounts_POST_AuthCode`.
2. Перейдите на вкладку **Authorization**.
3. Выберите тип **OAuth 2.0**.
4. Заполните параметры:
   - `Token Name`: `authcode_accesstoken`
   - `Grant Type`: `Authorization Code`
   - `Authorize using browser`: включите
   - `Auth URL`: `http://localhost:7080/realms/master/protocol/openid-connect/auth`
   - `Access Token URL`: `http://localhost:7080/realms/master/protocol/openid-connect/token`
   - `Client ID`: `eazybank-callcenter-ac`
   - `Client Secret`: `возьметё из keycloak-credentials`
   - `Scope`: `openid email profile`
   - `State`: `любая строчка для защиты от CSRF, например, ew34er-344fgfg-5gfgfg`
5. Нажмите **Get New Access Token**, войдите под пользователем (например, `user`/`password`), затем используйте полученный токен.

### Примеры запросов

**1. GET-запрос без аутентификации (PermitAll):**
```bash
GET http://localhost:8072/eazybank/accounts/api/contact-info
```
**Ответ:** Информация о контактах (публичный эндпоинт).

**2. GET-запрос с аутентификацией (через JWT-токен):**
```bash
GET http://localhost:8072/eazybank/accounts/api/fetchCustomerDetails?mobileNumber=4354437687
Authorization: Bearer {ваш-jwt-токен}
```

**3. POST-запрос с Client Credentials:**
```bash
POST http://localhost:8072/eazybank/accounts/api/create
Authorization: Bearer {токен-из-Client-Credentials}
Content-Type: application/json

{
    "name": "Madan Reddy",
    "email": "tutor@eazybytes",
    "mobileNumber": "4354437687"
}
```

**4. POST-запрос с Authorization Code (через браузер):**
```bash
POST http://localhost:8072/eazybank/cards/api/create?mobileNumber=4354437687
Authorization: Bearer {токен-из-Authorization-Code}
```

**5. Получение токена через Client Credentials (вручную):**
```bash
POST http://localhost:7080/realms/master/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=eazybank-callcenter-cc
&client_secret=берется из keycloak/credentials
&scope=openid email profile
```

## Структура проекта
```
bank_microservices/
├── accounts/                # Сервис управления счетами
├── cards/                   # Сервис управления картами
├── configserver/            # Центральный сервер конфигурации
├── docker-compose/          # Docker Compose файлы для различных окружений
│   └── prod/                # Файлы для продакшн-запуска
├── eurekaserver/            # Сервер Service Discovery (Eureka)
├── gatewayserver/           # API Gateway (Spring Cloud Gateway)
├── loans/                   # Сервис управления кредитами
├── message/                 # Сервис для асинхронной обработки сообщений
├── Endpoints for Postman.json  # Postman коллекция для тестирования API
└── README.md                # Этот файл
```

**Спасибо за интерес к проекту!**
