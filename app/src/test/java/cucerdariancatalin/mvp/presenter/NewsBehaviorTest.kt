package cucerdariancatalin.mvp.presenter

import io.kotlintest.Spec
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.lang3.reflect.FieldUtils

class NewsBehaviorTest : BehaviorSpec() {

    private lateinit var view: NewsViewContract
    private lateinit var repository: NewsRepository
    private lateinit var presenter: NewsPresenter
    private lateinit var throwable: Throwable
    private lateinit var newsResponse: NewsResponse
    private lateinit var schedulerProvider: ImmediateSchedulerProvider
    private lateinit var disposable: Disposable

    private fun setPrivateField(fieldName: String, value: Any?) {
        FieldUtils.writeDeclaredField(presenter, fieldName, value, true)
    }

    private fun getPrivateField(fieldName: String): Any? {
        return FieldUtils.readDeclaredField(presenter, fieldName, true)
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)

        view = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        throwable = mockk(relaxed = true)
        newsResponse = mockk(relaxed = true)
        schedulerProvider = mockk(relaxed = true)
        disposable = mockk(relaxed = true)

        presenter = NewsPresenter(repository, schedulerProvider)
    }

    init {
        val source = "techcrunch"
        given("I open news detail page with specific source") {
            presenter.setView(view)
            `when`("I see news detail page") {
                then("It should fetch news feed based on source") {
                    getPrivateField("view") shouldEqual view
                }
            }

            every { view.getCurrentSource() } returns source

            and("Failed when fetch news feed") {
                every {
                    repository.getNews(any())
                        .observeOn(schedulerProvider.ui())
                        .subscribeOn(schedulerProvider.io())
                } returns Single.error(throwable)

                `when`("I see news detail page with specific source") {
                    presenter.loadNews(source)

                    then("I will see error") {
                        verify(exactly = 1) { view.handleError() }
                    }
                }
            }

            and("Success when fetch news feed") {
                every {
                    repository.getNews(any())
                        .observeOn(schedulerProvider.ui())
                        .subscribeOn(schedulerProvider.io())
                } returns Single.just(newsResponse)

                `when`("I will see news detail page with specific source") {
                    presenter.loadNews(source)

                    then("I will see news feed loaded") {
                        verify(exactly = 1) { view.showNews(newsResponse) }
                    }
                }
            }

            and("I leave news detail page") {
                `when`("I leave news page") {
                    presenter.cancelLoadNews()

                    then("news feed cancel load") {
                        (getPrivateField("disposable") as Disposable).isDisposed shouldEqualTo true
                    }
                }
            }


        }
    }

}