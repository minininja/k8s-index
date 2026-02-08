import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet(name = "UndertowServletExample", urlPatterns = "/")
public class IndexServlet extends HttpServlet {

    @Inject
    protected KubernetesClient kubernetesClient;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body><h1>Services</h1>");

        String nodePortIp = null;
        for (NodeAddress addr : kubernetesClient.nodes().list().getItems().get(0).getStatus().getAddresses()) {
            if ("ExternalIP".equals(addr.getType())) {
                nodePortIp = addr.getAddress();
                break;
            }
        }

        String url;
        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            boolean header = false;
            List<Service> services  = kubernetesClient.services().inNamespace(ns.getMetadata().getName()).list().getItems();
            if (!services.isEmpty()) {
                for (Service service : services) {
                    switch(service.getSpec().getType()) {
                        case "LoadBalancer":
                            if (!header) {
                                out.write("<h2>" + ns.getMetadata().getName() + "</h2>");
                                header = true;
                            }
                            url = "http://" + service.getStatus().getLoadBalancer().getIngress().get(0).getIp() + ":" + service.getSpec().getPorts().get(0).getPort();
                            out.write("<li>");
                            out.write("<a href=\"" + url + "\">" + service.getMetadata().getName() + "</a>");
                            out.write("</li>");
                            break;
                        case "NodePort":
                            if (!header) {
                                out.write("<h2>" + ns.getMetadata().getName() + "</h2>");
                               header = true;
                            }
                            url = "http://" + nodePortIp + ":" + service.getSpec().getPorts().get(0).getNodePort();
                            out.write("<li>");
                            out.write("<a href=\"" + url + "\">" + service.getMetadata().getName() + "</a>");
                            out.write("</li>");
                            break;
                    }
                    out.write("</ul>");
                }
            }
        }
        out.println("</body></html>");

        out.close();
        out.flush();
    }
}
