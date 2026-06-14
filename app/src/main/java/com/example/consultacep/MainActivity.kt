package com.example.consultacep

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.Volley
import com.example.consultacep.databinding.ActivityMainBinding

/**
 * Tela principal do app.
 *
 * Responsabilidades:
 *  - Capturar o CEP digitado pelo usuario
 *  - Validar a entrada (campo vazio / formato)
 *  - Acionar a consulta via CepService
 *  - Exibir o resultado ou uma mensagem de erro amigavel
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cepService: CepService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // A fila de requisicoes do Volley executa as chamadas de rede
        // automaticamente fora da thread principal.
        cepService = CepService(Volley.newRequestQueue(this))

        binding.btnConsultar.setOnClickListener { realizarConsulta() }
    }

    /**
     * Le o CEP digitado, valida e dispara a consulta.
     */
    private fun realizarConsulta() {
        val cep = binding.etCep.text.toString().trim()

        // Validacao: campo vazio
        if (cep.isEmpty()) {
            binding.tilCep.error = getString(R.string.erro_campo_vazio)
            return
        }

        // Validacao: formato (a ViaCEP exige exatamente 8 digitos)
        if (cep.length != 8) {
            binding.tilCep.error = getString(R.string.erro_cep_invalido)
            return
        }

        // Limpa estado anterior antes de consultar
        binding.tilCep.error = null
        ocultarResultado()
        ocultarMensagem()
        exibirCarregando(true)

        cepService.consultar(
            cep = cep,
            aoSucesso = { endereco ->
                exibirCarregando(false)
                exibirResultado(endereco)
            },
            aoFalhar = { codigoErro ->
                exibirCarregando(false)
                val mensagem = when (codigoErro) {
                    "CEP_NAO_ENCONTRADO" -> getString(R.string.erro_cep_nao_encontrado)
                    else -> getString(R.string.erro_conexao)
                }
                exibirMensagem(mensagem)
            }
        )
    }

    /**
     * Preenche o card com os dados retornados e o torna visivel.
     */
    private fun exibirResultado(endereco: Endereco) {
        binding.tvLogradouro.text = ouTraco(endereco.logradouro)
        binding.tvBairro.text = ouTraco(endereco.bairro)
        binding.tvCidade.text = ouTraco(endereco.localidade)
        binding.tvEstado.text = ouTraco(endereco.uf)
        binding.tvDdd.text = ouTraco(endereco.ddd)
        binding.cardResultado.visibility = View.VISIBLE
    }

    /**
     * Alguns CEPs (ex.: de cidades pequenas) retornam logradouro vazio.
     * Nesses casos mostramos um traco em vez de um campo em branco.
     */
    private fun ouTraco(valor: String): String {
        return if (valor.isBlank()) "—" else valor
    }

    private fun exibirCarregando(carregando: Boolean) {
        binding.progressBar.visibility = if (carregando) View.VISIBLE else View.GONE
        binding.btnConsultar.isEnabled = !carregando
    }

    private fun exibirMensagem(texto: String) {
        binding.tvMensagem.text = texto
        binding.tvMensagem.visibility = View.VISIBLE
    }

    private fun ocultarMensagem() {
        binding.tvMensagem.visibility = View.GONE
    }

    private fun ocultarResultado() {
        binding.cardResultado.visibility = View.GONE
    }
}
