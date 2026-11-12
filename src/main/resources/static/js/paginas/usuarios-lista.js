const PERFIS = ['CLIENTE', 'TECNICO', 'ADMIN'];

let perfil = null;

paginaProtegida(async () => {
    perfil = new URLSearchParams(window.location.search).get('perfil');
    preencherSelect(document.getElementById('perfil'), PERFIS, null, null, perfil, 'Todos');

    document.getElementById('form-filtros').addEventListener('submit', evento => {
        evento.preventDefault();
        filtrar(dadosFormulario(evento.target).perfil);
    });
    document.getElementById('btn-limpar').addEventListener('click', () => {
        document.getElementById('form-filtros').reset();
        filtrar(null);
    });

    await carregar();
});

async function filtrar(novoPerfil) {
    perfil = novoPerfil;
    history.replaceState(null, '', '/app/usuarios' + queryString({ perfil }));
    limparMensagens();
    try {
        await carregar();
    } catch (e) {
        mostrarErro(e);
    }
}

async function carregar() {
    const usuarios = await api('GET', '/usuarios' + queryString({ perfil }));

    document.getElementById('vazio').hidden = usuarios.length > 0;
    document.getElementById('tabela').hidden = usuarios.length === 0;

    document.getElementById('lista-usuarios').innerHTML = usuarios.map(u =>
        '<tr>'
        + '<td>' + u.id + '</td>'
        + '<td><a href="/app/usuarios/' + u.id + '">' + esc(u.nome) + '</a></td>'
        + '<td>' + esc(u.email) + '</td>'
        + '<td>' + (u.telefone ? esc(u.telefone) : '<span class="suave">-</span>') + '</td>'
        + '<td>' + badge(u.perfil) + '</td>'
        + '<td>' + (u.ativo ? 'Sim' : 'Não') + '</td>'
        + '</tr>'
    ).join('');
}
