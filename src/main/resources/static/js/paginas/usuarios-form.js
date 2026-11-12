const PERFIS = ['CLIENTE', 'TECNICO', 'ADMIN'];
const id = document.getElementById('pagina').dataset.id;

paginaProtegida(async () => {
    const usuario = id ? await api('GET', '/usuarios/' + id) : null;

    const perfilSelect = document.getElementById('perfil');
    if (usuario) {
        preencherSelect(perfilSelect, PERFIS, null, null, usuario.perfil);
        document.getElementById('nome').value = usuario.nome;
        document.getElementById('email').value = usuario.email;
        document.getElementById('telefone').value = usuario.telefone || '';
        document.getElementById('dica-senha').hidden = false;
    } else {
        preencherSelect(perfilSelect, PERFIS, null, null, null, 'Selecione');
        perfilSelect.required = true;
        document.getElementById('senha').required = true;
    }

    document.getElementById('form-usuario').addEventListener('submit', salvar);
});

async function salvar(evento) {
    evento.preventDefault();
    limparMensagens();
    const dados = dadosFormulario(evento.target);

    const corpo = {
        nome: dados.nome,
        email: dados.email,
        senha: dados.senha,
        telefone: dados.telefone,
        perfil: Sessao.temPerfil('ADMIN') ? dados.perfil : null
    };

    const botao = document.getElementById('btn-salvar');
    botao.disabled = true;
    try {
        const salvo = id
            ? await api('PUT', '/usuarios/' + id, corpo)
            : await api('POST', '/usuarios', corpo);
        window.location.href = '/app/usuarios/' + salvo.id;
    } catch (e) {
        mostrarErro(e);
        botao.disabled = false;
    }
}
