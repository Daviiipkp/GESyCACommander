#!/bin/bash

# ==========================================
# 1. VARIÁVEIS (Altere conforme sua necessidade)
# ==========================================
URL_DO_PYTHON="https://api.daviipkp.org/startup.py" # Link direto para baixar seu .py
PASTA_DESTINO="/home/gesyca/startup"                  # Onde o arquivo vai ficar salvo
ARQUIVO_PYTHON="$PASTA_DESTINO/startup.py"         # Caminho completo do arquivo baixado
NOME_DO_SERVICO="startup.service"       # Nome do serviço no sistema

echo "==> Iniciando configuração..."

# ==========================================
# 2. CRIANDO A PASTA E BAIXANDO O ARQUIVO
# ==========================================
echo "==> Criando diretório $PASTA_DESTINO..."
mkdir -p "$PASTA_DESTINO"

echo "==> Baixando o script Python de $URL_DO_PYTHON..."
# O curl baixa o arquivo e o -o salva no caminho especificado
curl -o "$ARQUIVO_PYTHON" "$URL_DO_PYTHON"

# Dá permissão de execução para o arquivo
chmod +x "$ARQUIVO_PYTHON"

# ==========================================
# 3. CRIANDO O SERVIÇO NO SYSTEMD
# ==========================================
echo "==> Criando serviço de boot do systemd..."

# O comando 'cat <<EOF' escreve tudo até o 'EOF' dentro do arquivo de serviço
cat <<EOF > /etc/systemd/system/$NOME_DO_SERVICO
[Unit]
Description=Executa meu script Python no boot
# Garante que ele só rode depois que a internet (rede) estiver funcionando
After=network.target

[Service]
# Comando para rodar o Python (Pode precisar ajustar para /usr/bin/python dependendo do SO)
ExecStart=/usr/bin/python3 $ARQUIVO_PYTHON
# Se o script der erro e fechar, o sistema tenta abrir de novo automaticamente
Restart=always
# Roda como usuário root (se precisar de menos privilégios, mude aqui)
User=root

[Install]
WantedBy=multi-user.target
EOF

# ==========================================
# 4. HABILITANDO E INICIANDO O SERVIÇO
# ==========================================
echo "==> Recarregando o daemon do systemd..."
systemctl daemon-reload

echo "==> Habilitando o serviço para rodar no boot..."
systemctl enable "$NOME_DO_SERVICO"

echo "==> Iniciando o serviço agora mesmo..."
systemctl start "$NOME_DO_SERVICO"

echo "==> Concluído com sucesso! Para ver se está rodando, digite: systemctl status $NOME_DO_SERVICO"