git push -u origin develop

# Lista de funcionalidades
branches=(
  "feature/onboarding-e-termos"
  "feature/cadastro-de-usuario"
  "feature/validacao-de-email"
  "feature/salvar-uid-e-imei"
  "feature/login"
  "feature/gerenciar-senhas"
  "feature/criptografia-de-senhas"
  "feature/geracao-de-access-token"
  "feature/categorias-de-senhas"
  "feature/login-sem-senha"
  "feature/scan-qr-code"
  "feature/integracao-firebase-functions"
  "feature/status-login"
  "feature/recuperacao-senha-mestra"
  "feature/ui-ux"
  "feature/testes"
)

# Criar as branches
for branch in "${branches[@]}"; do
  git checkout develop
  git checkout -b "$branch"
  git push -u origin "$branch"
done

# Voltar pra develop no final
git checkout develop

echo "✅ Todas as branches foram criadas com sucesso!"
