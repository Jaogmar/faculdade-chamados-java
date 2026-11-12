document.addEventListener('DOMContentLoaded', () => {
    if (Sessao.valida()) {
        window.location.href = '/app';
        return;
    }

    if (new URLSearchParams(window.location.search).get('cadastrado') === '1') {
        mostrarSucesso('Cadastro realizado. Faça login.');
    }

    const form = document.getElementById('form-login');
    const botao = document.getElementById('btn-entrar');

    form.addEventListener('submit', async evento => {
        evento.preventDefault();
        limparMensagens();
        botao.disabled = true;
        try {
            const resposta = await api('POST', '/auth/login', dadosFormulario(form));
            await Sessao.salvar(resposta);
            window.location.href = '/app';
        } catch (e) {
            Sessao.limpar();
            mostrarErro(e);
            botao.disabled = false;
        }
    });
});
