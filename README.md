# MyApiTeste do ZARONIROD (FORK UTFPR)
Este projeto é um fork do repositório original para a entrega do trabalho da pós-graduação na UTFPR.

---

### ⚠️ AVISO IMPORTANTE: ARQUIVO DE CONFIGURAÇÃO
Por questões de segurança, o arquivo **`google-services.json`** (configuração do Firebase) foi adicionado ao `.gitignore` e **NÃO** está incluso neste repositório público.

**Para rodar este projeto, você deve:**
1. Criar seu próprio projeto no [Firebase Console](https://console.firebase.google.com/).
2. Adicionar um app Android com o package `com.example.myapitest`.
3. Baixar o arquivo `google-services.json` e colocá-lo na pasta **`app/`** do projeto manualmente.

---

### 🛠️ Check-list: O que fazer no Firebase Console
Para rodar a aplicação com sucesso, siga exatamente estes passos:

1. **Autenticação (Authentication)**
   - Vá em **Authentication > Sign-in method**.
   - Ative o provedor **Telefone**.
   - Cadastre o número de teste: `+55 11 91234-5678` com o código `123456`.

2. **Armazenamento (Storage)**
   - Ative o **Firebase Storage**.
   - Configure as Regras (Rules) para permitir leitura/escrita de usuários autenticados.

3. **Configurações do Projeto (Obrigatório para SMS)**
   - Vá na Engrenagem ⚙️ > **Configurações do projeto**.
   - Adicione as impressões digitais **SHA-1** e **SHA-256** da sua máquina.
   - *Comando para pegar os SHAs:* `./gradlew signingReport` ou `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`.

4. **Google Maps**
   - Habilite a **Maps SDK for Android** no Google Cloud Console.
   - Gere uma API Key e insira no `AndroidManifest.xml`.

---

### 🚀 Funcionalidades Implementadas (RZ)
- [x] Estrutura moderna em pacotes (`ui`, `data`, `repository`).
- [x] Login com Firebase Auth (Telefone).
- [x] Logout seguro limpando a stack de Activities.
- [x] Integração com API REST usando Retrofit e Coroutines.
- [x] Upload de fotos de carros para o Firebase Storage.
- [x] Listagem de carros com RecyclerView e Picasso.
- [x] Visualização de localização no Google Maps.
- [x] Interface atualizada com Material Design 3 e UX aprimorada.

---

**RZ - Projeto revisado, refatorado e pronto para avaliação.**
