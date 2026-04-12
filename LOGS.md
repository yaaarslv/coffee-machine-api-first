# Логи
## Принцип сбора
1. Приложение пишет логи в stdout
2. Promtail собирает docker-логи контейнера
3. Loki хранит и индексирует логи
4. Grafana используется для визуализации и анализа

![img.png](static/logs/img.png)
## Примеры запросов
### Бизнес-события
```
{container="coffee-app"} |= "BUSINESS"
```
![img_1.png](static/logs/img_1.png)

## Предупреждения
```
{container="coffee-app"} |= "WARN" |= "reason="
```
![img_4.png](static/logs/img_4.png)

## Ошибки
```
{container="coffee-app"} |= "ERROR"
```
![img_5.png](static/logs/img_5.png)

## Активность системы (события/мин)
```
sum(count_over_time({container="coffee-app"} |= "BUSINESS" [1m]))
```
![img_2.png](static/logs/img_2.png)

## Все логи
```
{container="coffee-app"} |= ``
```
![img_3.png](static/logs/img_3.png)