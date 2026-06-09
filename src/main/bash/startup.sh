#!/bin/bash

# ==========================================
# VARIÁVEIS PRINCIPAIS
# ==========================================
NOME_DO_SERVICO="meu_startup.service"
DIRETORIO="/opt/meu_startup"
ARQUIVO_PYTHON="$DIRETORIO/startup.py"
URL_API="https://api.daviipkp.org/startup.py"

echo "==> Iniciando configuração automática..."

# ==========================================
# 1. VERIFICANDO E INSTALANDO O PYTHON 3
# ==========================================
echo "==> Verificando dependências: Python 3..."
if ! command -v python3 &> /dev/null; then
    echo "    Python 3 não encontrado. Iniciando a instalação..."
    apt-get update -qq
    apt-get install -y python3
    if [ $? -ne 0 ]; then
        echo "    ERRO: Falha ao instalar o Python 3."
        exit 1
    fi
    echo "    Python 3 instalado com sucesso."
else
    echo "    Python 3 já está instalado."
fi

# ==========================================
# 2. BAIXANDO O SCRIPT PYTHON
# ==========================================
echo "==> Preparando diretório em $DIRETORIO..."
mkdir -p "$DIRETORIO"

echo "==> Baixando/Atualizando o arquivo Python da API..."
wget -qO "$ARQUIVO_PYTHON" "$URL_API"

if [ $? -ne 0 ]; then
    echo "    ERRO: Falha ao baixar o startup.py. Verifique a URL."
    exit 1
fi

# Garante permissão de leitura e execução
chmod 755 "$ARQUIVO_PYTHON"
echo "    Download concluído com sucesso."

# ==========================================
# 3. CRIANDO O SERVIÇO NO SYSTEMD
# ==========================================
echo "==> Criando serviço de boot do systemd à prova de falhas de rede..."

# Este bloco sobrescreve o arquivo do serviço com as regras corretas de rede e reinício
cat <<EOF > /etc/systemd/system/$NOME_DO_SERVICO
[Unit]
Description=Executa meu script Python no boot
After=network-online.target
Wants=network-online.target
# Desativa o limite de desistência do systemd (Start request repeated too quickly)
StartLimitIntervalSec=0

[Service]
Type=simple
ExecStart=/usr/bin/python3 $ARQUIVO_PYTHON

Restart=on-failure
# OBRIGATÓRIO: Espera 10 segundos antes de tentar de novo. Dá tempo da rede conectar.
RestartSec=10

User=root

[Install]
WantedBy=multi-user.target
EOF

# ==========================================
# 4. APLICANDO E INICIANDO
# ==========================================
echo "==> Recarregando o systemd..."
systemctl daemon-reload

echo "==> Habilitando para iniciar no boot..."
systemctl enable $NOME_DO_SERVICO

echo "==> Iniciando o serviço agora..."
systemctl restart $NOME_DO_SERVICO

echo "DONE! Configuração concluída."