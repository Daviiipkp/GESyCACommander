# ==========================================
# 3. CRIANDO O SERVIÇO NO SYSTEMD (ATUALIZADO)
# ==========================================
echo "==> Criando serviço de boot do systemd..."

cat <<EOF > /etc/systemd/system/$NOME_DO_SERVICO
[Unit]
Description=Executa meu script Python no boot apenas uma vez
After=network.target

[Service]
# Diz ao Linux que é um script de execução única
Type=oneshot

ExecStart=/usr/bin/python3 $ARQUIVO_PYTHON

# Tenta rodar de novo APENAS se o script der algum erro (crash). 
# Se ele finalizar com sucesso (exit 0), não faz nada.
Restart=on-failure

User=root

[Install]
WantedBy=multi-user.target
EOF
echo "DONE"