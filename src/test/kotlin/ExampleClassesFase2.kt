@Tag("componente")
data class ComponenteAvaliacao (
    @TagAttribute
    val nome: String,

    @TagAttribute
    val peso: Int)

data class FUC (
    @TagAttribute
    val codigo: String,

    val nome: String,
    val ects: Double,
    val observacoes: String,
    val avaliacao: List<ComponenteAvaliacao>
)

