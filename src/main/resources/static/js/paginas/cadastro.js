document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-cadastro');
    const botao = document.getElementById('btn-cadastrar');

    form.addEventListener('submit', async evento => {
        evento.preventDefault();
        limparMensagens();
        botao.disabled = true;
        try {
            await api('POST', '/auth/cadastro', dadosFormulario(form));
            window.location.href = '/app/login?cadastrado=1';
        } catch (e) {
            mostrarErro(e);
            botao.disabled = false;
        }
    });
});
