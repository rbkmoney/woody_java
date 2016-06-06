# com.rbkmoney.woody.api

Java API, предоставляющее основу для реализации сквозной трассировки событий в распределенной системе.  
Основан на thread-context подходе (информация о цепочке хранится в контексте, который привязан к потоку, в котором началось выполнение или обработка запроса).

Из чего состоит контекст:

1. *Client Span* - информация о цепочке для клиентского вызова (запроса).
2. *Service Span* - информация о цепочке при обработке входящего вызова (запроса) для сервиса.

__Span__ - содержит информацию о текущей цепочке (`trace_id`,  `span_id`, `parent_id`, `duration` и т.д.), а так же `metadata`:


- `trace_id` - id всей цепочки, должен быть уникальным в пределах всей системы, по крайней мере стараться им быть.
-  `span_id` - id текущего вызова. Создается для всех клиентских запросов, для входящих запросов используется переданное значение.
-  `parent_id` - id родительского вызова, переданное значение используется для дочерних клиентских запросов.
-  `metadata` - метаданные запроса, эти данные не передаются при вызове внешних сервисов. Содержат информацию о вызове, транспорте, ошибках и другую информацию woody-api и его реализации.

__Пример получения текущего контекста:__

```java
com.rbkmoney.woody.api.trace.context.TraceContext.getCurrentTraceData(); 
``` 

При разработке сервиса иногда возникает необходимость обработать  запросы в потоке, отличном от того, который принял запрос и владеет информацией о контексте вызова. К примеру сложить их в очередь и обрабатывать в пуле. Для этих целей в woody-api есть соответствующий пакет:
 `com.rbkmoney.woody.api.concurrent`
 
 С его помощъю можно создать объекты `Runnable` и `Callable`, которые будут использовать указанный контекст, либо текущий контекст потока, если он не указан явно.
 
 __Пример создания `Runnable`:__
 
 ```java
 Runnable workRunnable = ...;//real work is defined here
 Runnable woodyRunnable = WRunnable.ceate(workRunnable); //wrap our work in woody context
 new Thread(woodyRunnable).start(); //run work with current woody context in other thread
 ``` 
Объект `Callable` создается аналогичным образом с помощью `WCallable.create(...);`.

 Можно переложить эту задачу на `WExecutorExecutor`, тогда все задания, которые в него кладутся, автоматически получат контекст потока, который их добавлял.  
 
__Пример создания `ExecutorService`:__
 
```java
Executor executor = new WExecutorService(yourExecutorService);
executor.submit(yourCallable); //this task will be executed with woody context of current thread.
```

__Создание форков:__

Если по каким-либо причинам нужно создать новый контекст, полностью отвязав его от текущего контекста, это можно сделать следующим образом:

```java
WRunnable.createFork(yourForkedRunnable).run(); // All requests here're root requests
```
	
Аналогично для `Callable`:  
`WCallable.createFork(yourForkedCallable).call();`.