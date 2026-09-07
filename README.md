#  O Pente Fino (Motor de Malha Fina Fiscal)

Um sistema de auditoria fiscal automatizado focado no cruzamento de dados tributários e detecção de infrações patrimoniais. Desenvolvido em **Java**, o projeto simula a inteligência de negócios utilizada em malhas finas corporativas e governamentais, aplicando validações automatizadas e gerando autos de infração em formato PDF.

##  Arquitetura e Padrões de Projeto

O núcleo do sistema foi construído visando alta coesão e baixo acoplamento:
* **Padrão Strategy:** O motor de regras de negócio é totalmente desacoplado. Novas infrações fiscais podem ser conectadas ao sistema sem alterar a classe principal de processamento (`MalhaFinaService`).
* **Padrão DAO (Data Access Object):** Isolamento da camada de persistência.
* **Test-Driven Development (TDD):** Validação matemática do motor de regras blindada por testes unitários e de integração utilizando um banco de dados em memória, garantindo a integridade do sistema a cada nova versão.

##  Tecnologias Utilizadas

* **Linguagem:** Java (Records, Streams, Orientação a Objetos avançada)
* **Interface Gráfica:** JavaFX
* **Banco de Dados (Produção):** MySQL
* **Banco de Dados (Testes):** H2 Database (In-Memory)
* **Connection Pool:** HikariCP
* **Testes Automatizados:** JUnit 5 / Maven Surefire
* **Geração de Relatórios:** iTextPDF
* **Gerenciamento de Dependências:** Maven

##  Malhas Fiscais Implementadas

O motor de auditoria processa as entidades `Dipj` (Declaração de Receitas e Despesas), `Dimof` (Movimentação Bancária) e `Bem` (Aquisições Patrimoniais), aplicando atualmente as seguintes regras:

1. **Omissão de Receita (Sonegação):** Dispara quando o volume transacionado nas contas bancárias supera a receita bruta declarada no exercício.
2. **Variação Patrimonial a Descoberto:** Detecta aquisição de bens incompatíveis com a soma da receita declarada, evidenciando acréscimo patrimonial sem origem lícita comprovada.
3. **Distribuição Disfarçada de Lucros (DDL):** Cruza a aquisição de bens patrimoniais contra o lucro líquido contábil, detectando confusão patrimonial e uso indevido da PJ para custeio pessoal de sócios.

##  Como Executar o Projeto

**1. Clonar o repositório**
```bash
git clone [https://github.com/seu-usuario/projeto-pente-fino.git](https://github.com/seu-usuario/projeto-pente-fino.git)
cd projeto-pente-fino