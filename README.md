#  Projeto Pente Fino - Motor de Malha Fiscal

> Sistema de Auditoria Digital e Detecção de Fraudes Tributárias desenvolvido em Java.

![Status](https://img.shields.io/badge/Status-Concluído-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![Tech](https://img.shields.io/badge/Tech-JavaFX%20%7C%20MySQL%20%7C%20JUnit-blue)

##  Sobre o Projeto

Este projeto simula o núcleo de processamento da **Malha Fina da Receita Federal**. O objetivo é cruzar dados de múltiplas fontes (Declarações Fiscais, Informes Bancários e Registro de Bens) para identificar automaticamente inconsistências e indícios de sonegação fiscal.

O sistema foi desenvolvido com foco em **Arquitetura em Camadas**, **Design Patterns** (DAO) e **Clean Code**, simulando um ambiente corporativo real.

##  Funcionalidades Principais

* **Persistência de Dados:** Integração completa com banco de dados **MySQL** utilizando o padrão **DAO (Data Access Object)** e JDBC.
* **Motor de Regras (Core):**
    *  *Cruzamento Bancário:* Identifica receitas não declaradas comparando extratos x declarações.
    *  *Variação Patrimonial a Descoberto:* Identifica compras de bens sem lastro financeiro (Sinais Exteriores de Riqueza).
* **Geração de Documentos:** Emissão automática de **Auto de Infração em PDF** com cálculo de multas e fundamentação legal.
* **Simulador Manual:** Interface para inserção de dados em tempo real para testar cenários de fraude e salvamento no banco.
* **Dashboard Executivo:** Interface gráfica (JavaFX) com gráficos estatísticos e identidade visual governamental.

##  Tecnologias Utilizadas

* **Linguagem:** Java 17 (LTS)
* **Interface:** JavaFX 21 + CSS (Estilização Gov.br)
* **Banco de Dados:** MySQL 8.0
* **Build Tool:** Maven
* **Testes:** JUnit 5 (Cobertura de regras de negócio)
* **Libs:** iText (PDF Engine), Lombok, MySQL Connector.

##  Lógica de Auditoria (Exemplo)

O sistema aplica a seguinte lógica para Variação Patrimonial:

```java
BigDecimal lucroDisponivel = receita.subtract(despesa);
BigDecimal gastosBens = bens.stream().map(Bem::valor).reduce(BigDecimal::add);

if (gastosBens.compareTo(lucroDisponivel) > 0) {
    throw new SonegacaoException("Variação Patrimonial a Descoberto");
}