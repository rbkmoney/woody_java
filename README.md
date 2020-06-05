# com.rbkmoney.woody

[![Total alerts](https://img.shields.io/lgtm/alerts/g/rbkmoney/woody_java.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/rbkmoney/woody_java/alerts/)

Java реализация [Библиотеки RPC вызовов для общения между микросервисами](http://52.29.202.218/design/ms/platform/rpc-lib/).

## Описание

1. [woody-api](woody-api/woody-api.md)
1. [woody-thrift](woody-thrift/woody-thrift.md)

## Для ознакомления

[Thrift](https://thrift.apache.org/)  
[Dapper](http://research.google.com/pubs/pub36356.html)

## Выпуск новой версии
Версии _woody-pom_ и всех его модулей должны совпадать, для этого перед началом работы над новой версией библиотеки нужно увеличить версию _woody-pom_ и в корневой директории проекта выполнить команду:  
`mvn versions:update-child-modules -DgenerateBackupPoms=false`  
Параметр `generateBackupPoms` можно опустить, если нужны резервные копии изменяемых файлов.
