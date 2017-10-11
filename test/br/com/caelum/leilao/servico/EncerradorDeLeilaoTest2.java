package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest2 {
	
	private RepositorioDeLeiloes daoFalso;
	private EnviadorDeEmail carteiroFalso;
	
	@Before
	public void init() {
		//dao
		this.daoFalso = mock(RepositorioDeLeiloes.class);
		//carteiro
		this.carteiroFalso = mock(EnviadorDeEmail.class);
	}
	
	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Carro Clássico").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao2 encerrador = new EncerradorDeLeilao2(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		assertEquals(2, encerrador.getTotalEncerrados());
		
	}
	
	@Test
	public void naoDeveEncerrarLeiloesDaSemanaCorrente() {
		
		Calendar antiga = Calendar.getInstance();
		antiga.set(2017, 10, 9);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Carro Clássico").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao2 encerrador = new EncerradorDeLeilao2(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());
		assertEquals(0, encerrador.getTotalEncerrados());
		
		verify(daoFalso, never()).atualiza(leilao1);
		verify(daoFalso, never()).atualiza(leilao1);
		
	}
	
	@Test
	public void naoDeveEncerrarQuandoNaoHouverLeiloes() {
		
		EncerradorDeLeilao2 encerrador = new EncerradorDeLeilao2(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());		
	}
	
	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(antiga).constroi();
        
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
        EncerradorDeLeilao2 encerrador = new EncerradorDeLeilao2(daoFalso, carteiroFalso);
        encerrador.encerra();
        
        // verificando que o metodo atualiza foi realmente invocado!
        //verify(daoFalso, times(1)).atualiza(leilao1);
        
        // passamos os mocks que serao verificados
        InOrder inOrder = inOrder(daoFalso, carteiroFalso);
        // a primeira invocação
        inOrder.verify(daoFalso, times(1)).atualiza(leilao1);    
        // a segunda invocação
        inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
        
	}
	
	@Test
	public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
				.naData(antiga).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EncerradorDeLeilao2 encerrador = new EncerradorDeLeilao2(daoFalso, carteiroFalso);

		encerrador.encerra();

		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso, times(1)).atualiza(leilao1);    
		inOrder.verify(carteiroFalso, times(1)).envia(leilao1);    
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoDaoFalha() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
				.naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
				.naData(antiga).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		doThrow(new RuntimeException()).when(carteiroFalso).envia(leilao1);

		EncerradorDeLeilao2 encerrador = 
				new EncerradorDeLeilao2(daoFalso, carteiroFalso);

		encerrador.encerra();

		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
	}
	
	@Test
	public void naoDeveExecutarQuandoTodosFalharem() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
				.naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
				.naData(antiga).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		/*doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao2);
		*/
		doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
		
		EncerradorDeLeilao2 encerrador = 
				new EncerradorDeLeilao2(daoFalso, carteiroFalso);

		encerrador.encerra();

		/*verify(carteiroFalso, never()).envia(leilao1);
		verify(carteiroFalso, never()).envia(leilao2);*/
		verify(carteiroFalso, never()).envia(any(Leilao.class));
	}
	
}
