package scripts.api.impl.subsystems.deployment

import org.craftercms.cstudio.api.service.deployment.DeploymentService;

class DeploymentServiceFacade {

    def context = null

    /**
     * constructor
     *
     * @param context
     *          service context
     */
    public DeploymentServiceFacade(context) {
        this.context = context
    }

    protected getDeploymentServiceImpl() {
        return context.getBean("jmsConnectionFactory")
    }

    public getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType) {

    }
}
