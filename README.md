# E-Books Library
Мобільний додаток, розроблений для реалізації віртуальної книжкової <s>і не тільки</s> бібліотеки.  

Виконав: Матненко Станіслав, АІ-202
## Використані технології:
- Android
- Kotlin
- Firebase
## Засоби Firebase:
- Authentication - аутентифіцікація за поштою/паролем;
- Realtime Database - робота з даними користувачів, книг та їх категорій;
- Storage - зберігання PDF-файлів та фотографій.
## Функціонал додатка:
### Початок роботи з додатком
- Авторизація чи створення нового облікового запису із використанням пошти/паролю;
- Можливість відновлення паролю у разі потреби;
- Вхід у додаток без авторизації (закриває доступ до певної частини функціоналу).
### Користувачі
#### Ролі користувачів
- Дві ролі - *Звичайний користувач* (User) чи *Адміністратор* (Admin);
- Кожен створений користувач автоматично стає Звичайним.  
Визначення Адміністратора можливе лише для того, хто має прямий доступ до БД;
- Ролі різняться між собою функціоналом та візуальним стилем додатку.
#### Профіль користувача
- Недоступний при вході без авторизації;
- Профіль відображає основну інформацію про користувача;
- Профіль може бути редагований, зокрема - можливе встановлення фотографії профілю.  
Для цього може бути використана або камера, або галерея.
### Бібліотека
Книги у бібліотеці розбиті за певними категоріями, які можуть бути визначені лише адміністратором додатка.
#### Категорії (функціонал Admin'а)
- Додавання нової категорії;
- Редагування чи видалення вже існуючої категорії;
- Прямий пошук потрібної категорії.
#### Книги (функціонал Admin'а)
- Додавання нової книги разом із PDF-файлом;
- Редагування чи видалення вже існуючої книги.
#### Книги (загальнодоступний функціонал)
- Одержання книг певної категорії;
- Пошук книг;
- Перегляд інформації про книгу;
- Перегляд книги у форматі PDF прямо у додатку;
- Завантаження PDF-файлу книги;
- Додавання/видалення книги до/зі списку улюблених книг;
Улюблені книги відображаються у профілі користувача.
## Скріншоти демонстрації роботи додатка:
### Головний екран/Реєстрація  
![Демонстрація1](https://github.com/ThaumBear/E-BooksLibrary/blob/main/screenshots/%D0%90%D0%BD%D0%B4%D1%80%D0%BE%D0%B8%D0%B41.png)  
### Авторизація/Зміна паролю  
![Демонстрація2](https://github.com/ThaumBear/E-BooksLibrary/blob/main/screenshots/%D0%90%D0%BD%D0%B4%D1%80%D0%BE%D0%B8%D0%B42.png) 
### Список книг/Перегляд даних про книгу/Перегляд PDF-файлу книги  
![Демонстрація3](https://github.com/ThaumBear/E-BooksLibrary/blob/main/screenshots/%D0%90%D0%BD%D0%B4%D1%80%D0%BE%D0%B8%D0%B43.png)  
### Профіль користувача/Редагування профілю  
![Демонстрація4](https://github.com/ThaumBear/E-BooksLibrary/blob/main/screenshots/%D0%90%D0%BD%D0%B4%D1%80%D0%BE%D0%B8%D0%B44.png)  
### Пошук категорії/Обрання дії/Редагування категорії  
![Демонстрація5](https://github.com/ThaumBear/E-BooksLibrary/blob/main/screenshots/%D0%90%D0%BD%D0%B4%D1%80%D0%BE%D0%B8%D0%B45.png)  
