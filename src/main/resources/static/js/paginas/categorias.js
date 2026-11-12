let categorias = [];

paginaProtegida(async () => {
    const form = document.getElementById('form-categoria');
    form.addEventListener('submit', salvar);
    document.getElementById('btn-cancelar').addEventListener('click', limparFormulario);
    document.getElementById('lista-categorias').addEventListener('click', cliqueNaTabela);

    await carregarCategorias();
});

async function carregarCategorias() {
    categorias = await api('GET', '/categorias');

    const vazio = categorias.length === 0;
    document.getElementById('vazio').hidden = !vazio;
    document.getElementById('tabela-categorias').hidden = vazio;

    document.getElementById('lista-categorias').innerHTML = categorias.map(c => '<tr>'
        + '<td>' + c.id + '</td>'
        + '<td>' + esc(c.nome) + '</td>'
        + '<td>' + (c.descricao ? esc(c.descricao) : '-') + '</td>'
        + '<td data-perfil="ADMIN"><div class="acoes">'
        + '<button type="button" class="btn btn-secundario" data-acao="editar" data-id="' + c.id + '">Editar</button>'
        + '<button type="button" class="btn btn-perigo" data-acao="excluir" data-id="' + c.id + '">Excluir</button>'
        + '</div></td>'
        + '</tr>').join('');

    aplicarPerfis();
}

async function salvar(evento) {
    evento.preventDefault();
    limparMensagens();

    const dados = dadosFormulario(evento.target);
    const corpo = { nome: dados.nome, descricao: dados.descricao };
    const botao = document.getElementById('btn-salvar');
    botao.disabled = true;
    try {
        if (dados.id) {
            await api('PUT', '/categorias/' + Number(dados.id), corpo);
            mostrarSucesso('Categoria atualizada com sucesso.');
        } else {
            await api('POST', '/categorias', corpo);
            mostrarSucesso('Categoria criada com sucesso.');
        }
        limparFormulario();
        await carregarCategorias();
    } catch (e) {
        mostrarErro(e);
    } finally {
        botao.disabled = false;
    }
}

function cliqueNaTabela(evento) {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    const id = Number(botao.dataset.id);
    if (botao.dataset.acao === 'editar') {
        editar(id);
    } else {
        excluir(botao, id);
    }
}

function editar(id) {
    const categoria = categorias.find(c => c.id === id);
    if (!categoria) return;
    limparMensagens();
    document.getElementById('id').value = categoria.id;
    document.getElementById('nome').value = categoria.nome;
    document.getElementById('descricao').value = categoria.descricao || '';
    document.getElementById('titulo-form').textContent = 'Editar categoria';
    document.getElementById('btn-cancelar').hidden = false;
    document.getElementById('nome').focus();
    window.scrollTo(0, 0);
}

function limparFormulario() {
    document.getElementById('form-categoria').reset();
    document.getElementById('id').value = '';
    document.getElementById('titulo-form').textContent = 'Nova categoria';
    document.getElementById('btn-cancelar').hidden = true;
}

async function excluir(botao, id) {
    if (botao.dataset.confirmar !== 'sim') {
        botao.dataset.confirmar = 'sim';
        botao.textContent = 'Confirmar';
        return;
    }

    limparMensagens();
    botao.disabled = true;
    try {
        await api('DELETE', '/categorias/' + id);
        if (document.getElementById('id').value === String(id)) {
            limparFormulario();
        }
        mostrarSucesso('Categoria excluída com sucesso.');
        await carregarCategorias();
    } catch (e) {
        if (!e || !e.erro) {
            e = { erro: 'Não foi possível excluir a categoria.',
                  detalhes: ['Verifique se ela está sendo usada por algum chamado.'] };
        }
        mostrarErro(e);
        botao.disabled = false;
        botao.dataset.confirmar = '';
        botao.textContent = 'Excluir';
    }
}
