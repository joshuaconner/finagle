package com.twitter.finagle.client

import com.twitter.finagle.service
import com.twitter.util.Duration
import com.twitter.util.tunable.Tunable

/**
 * @note This interface primarily exists to share Scaladocs across
 *       protocol-specific `MethodBuilder` implementations.
 */
private[finagle] trait MethodBuilderScaladoc[T] {

  /**
   * Set a total timeout, including time spent on retries.
   *
   * If the request does not complete in this time, the response
   * will be satisfied with a [[com.twitter.finagle.GlobalRequestTimeoutException]].
   *
   * Defaults to using the client's configuration for
   * [[com.twitter.finagle.service.TimeoutFilter.TotalTimeout(timeout)]].
   *
   * @example
   * For example, a total timeout of 200 milliseconds:
   * {{{
   * import com.twitter.conversions.time._
   * import com.twitter.finagle.http.Http
   * import com.twitter.util.Duration
   *
   * val client: Http.Client = ???
   * val builder = client.methodBuilder("inet!example.com:80")
   * builder.withTimeoutTotal(200.milliseconds))
   * }}}
   *
   * @param howLong how long, from the initial request issuance,
   *                is the request given to complete.
   *                If it is not finite (e.g. `Duration.Top`),
   *                no method specific timeout will be applied.
   *
   * @see [[withTimeoutPerRequest(Duration)]]
   *
   * @return a new instance with all other settings copied
   */
  def withTimeoutTotal(howLong: Duration): T

  /**
   * Set a total timeout with a [[Tunable]], including time spent on retries.
   *
   * If the request does not complete in this time, the response
   * will be satisfied with a [[com.twitter.finagle.GlobalRequestTimeoutException]].
   *
   * Defaults to using the client's configuration for
   * [[com.twitter.finagle.service.TimeoutFilter.TotalTimeout(timeout)]].
   *
   * @example
   * For example, a total timeout of 200 milliseconds:
   * {{{
   * import com.twitter.conversions.time._
   * import com.twitter.finagle.http.Http
   * import com.twitter.util.Duration
   * import com.twitter.util.tunable.Tunable
   *
   * val client: Http.Client = ???
   * val tunableTimeout: Tunable[Duration] = Tunable.const("id", 200.milliseconds)
   * val builder = client.methodBuilder("inet!example.com:80")
   * builder.withTimeoutTotal(tunableTimeout))
   * }}}
   *
   * @param howLong how long, from the initial request issuance,
   *                is the request given to complete.
   *                If it is not finite (e.g. `Duration.Top`),
   *                no method specific timeout will be applied.
   *
   * @see [[withTimeoutPerRequest(Tunable[Duration])]]
   *
   * @return a new instance with all other settings copied
   */
  def withTimeoutTotal(howLong: Tunable[Duration]): T

  /**
   * How long a '''single''' request is given to complete.
   *
   * If there are [[withRetryForClassifier retries]], each attempt is given up
   * to this amount of time.
   *
   * If a request does not complete within this time, the response
   * will be satisfied with a [[com.twitter.finagle.IndividualRequestTimeoutException]].
   *
   * Defaults to using the client's configuration for
   * [[com.twitter.finagle.service.TimeoutFilter.Param(timeout)]],
   * which is typically set via
   * [[com.twitter.finagle.param.CommonParams.withRequestTimeout]].
   *
   * @example
   * For example, a per-request timeout of 50 milliseconds:
   * {{{
   * import com.twitter.conversions.time._
   * import com.twitter.finagle.http.Http
   * import com.twitter.util.Duration
   *
   * val client: Http.Client = ???
   * val builder = client.methodBuilder("inet!example.com:80")
   * builder.withTimeoutPerRequest(50.milliseconds))
   * }}}
   *
   * @param howLong how long, from the initial request issuance,
   *                an individual attempt given to complete.
   *                If it is not finite (e.g. `Duration.Top`),
   *                no method specific timeout will be applied.
   *
   * @see [[withTimeoutTotal(Duration)]]
   *
   * @return a new instance with all other settings copied
   */
  def withTimeoutPerRequest(howLong: Duration): T

  /**
   * How long a '''single''' request is given to complete.
   *
   * If there are [[withRetryForClassifier retries]], each attempt is given up
   * to this amount of time.
   *
   * If a request does not complete within this time, the response
   * will be satisfied with a [[com.twitter.finagle.IndividualRequestTimeoutException]].
   *
   * Defaults to using the client's configuration for
   * [[com.twitter.finagle.service.TimeoutFilter.Param(timeout)]],
   * which is typically set via
   * [[com.twitter.finagle.param.CommonParams.withRequestTimeout]].
   *
   * @example
   * For example, a per-request timeout of 50 milliseconds:
   * {{{
   * import com.twitter.conversions.time._
   * import com.twitter.finagle.http.Http
   * import com.twitter.util.Duration
   * import com.twitter.util.tunable.Tunable
   *
   * val client: Http.Client = ???
   * val tunableTimeout: Tunable[Duration] = Tunable.const("id", 50.milliseconds)
   * val builder = client.methodBuilder("inet!example.com:80")
   * builder.withTimeoutPerRequest(tunableTimeout))
   * }}}
   *
   * @param howLong how long, from the initial request issuance,
   *                an individual attempt given to complete.
   *                If it is not finite (e.g. `Duration.Top`),
   *                no method specific timeout will be applied.
   *
   * @see [[withTimeoutTotal(Tunable[Duration])]]
   *
   * @return a new instance with all other settings copied
   */
  def withTimeoutPerRequest(howLong: Tunable[Duration]): T

  /**
   * Retry based on [[ResponseClassifier]].
   *
   * The default behavior is to use the client's classifier which is typically
   * configured through `theClient.withResponseClassifier` or
   * `ClientBuilder.withResponseClassifier`.
   *
   * This classifier is used to determine which requests are unsuccessful.
   * This is the basis for measuring the logical success metrics as well as
   * logging unsuccessful requests at debug level.
   *
   * @example
   * For example, retrying on a 418 status code:
   * {{{
   * import com.twitter.conversions.time._
   * import com.twitter.finagle.http.Http
   * import com.twitter.finagle.service.{ReqRep, ResponseClass}
   * import com.twitter.util.Return
   *
   * val client: Http.Client = ???
   * val builder = client.methodBuilder("inet!example.com:80")
   * builder.withRetryForClassifier {
   *   case ReqRep(_, Return(rep)) if rep.statusCode == 418 => ResponseClass.RetryableFailure
   * }
   * }}}
   *
   * The classifier is also used to determine the logical success metrics of
   * the client.
   *
   * @param classifier when a [[com.twitter.finagle.service.ResponseClass.Failed Failed]]
   *                   with `retryable` is `true` is returned for a given `ReqRep`, the
   *                   request will be retried. This is often a
   *                   `ResponseClass.RetryableFailure`.
   *
   * @see [[withRetryDisabled]]
   *
   * @see [[https://twitter.github.io/finagle/guide/Clients.html#response-classification
   *     response classification user guide]]
   */
  def withRetryForClassifier(classifier: service.ResponseClassifier): T

  /**
   * Disables "application" level retries.
   *
   * This '''does not''' disable retries of failures that are known
   * to be safe to retry via `com.twitter.finagle.service.RequeueFilter`.
   *
   * This causes the logical success metrics to be based on the
   * [[ResponseClassifier.Default default response classifier]] rules
   * of a `Return` response is a success, while everything else is not.
   *
   * @example
   * {{{
   * import com.twitter.finagle.http.Http
   *
   * val client: Http.Client = ???
   * val builder = client.methodBuilder("inet!example.com:80")
   * builder.withRetryDisabled
   * }}}
   *
   * @see [[withRetryForClassifier]]
   */
  def withRetryDisabled: T


  /**
   * Configure that requests are to be treated as idempotent. Because requests can be safely
   * retried, [[BackupRequestFilter]] is configured with the params `maxExtraLoad` and
   * `sendInterrupts` to decrease tail latency by sending an additional fraction of requests.
   *
   * @param maxExtraLoad How much extra load, as a fraction, we are willing to send to the server.
   *                     Must be between 0.0 and 1.0.
   */
  def idempotent(
    maxExtraLoad: Double
  ): T

  /**
   * Configure that requests are to be treated as non-idempotent. [[BackupRequestFilter]] is
   * disabled, and only those failures that are known to be safe to retry (i.e., write failures,
   * where the request was never sent) are retried via requeue filter; any previously configured
   * retries are removed.
   */
  def nonIdempotent: T

}
